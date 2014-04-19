package com.amap.cp.client;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.utils.*;
import com.amap.cp.save.AbstractSave;
import com.google.common.collect.*;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 多线程 cp 处理
 * Created by yang.hua on 14-1-21.
 */
public class MultiThreadExecutor extends Thread {
    private final Logger logger = LoggerFactory.getLogger(MultiThreadExecutor.class);
    private PropertiesConfiguration conf = Utils.COMM_CONF;
    /*System processor counts*/
    private int proc = conf.getInt("threadCount");
    /*每次查询poiid的数量*/
    private int initPoiidCount = conf.getInt("initPoiidCount");
    private String cpName;
    private AbstractSave cpProcessor = null;
    /*添加任务线程*/
    private FetchPoiIds fetchPoiIds;
    /*更新数据库线程*/
    private ExecuteUpdate batchUpdate;
    /*初始化待处理cp,创建对应处理程序的实例*/
    public MultiThreadExecutor(String cpName, int threadCount) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        this.cpName = cpName;
        if (threadCount > 0) {
            this.proc = threadCount;
            logger.info("Thread-Counts=[{}]", threadCount);
        }
        ClassLoader loader = MultiThreadExecutor.class.getClassLoader();
        cpProcessor = (AbstractSave) loader.loadClass(conf.getString(cpName + ".class")).newInstance();
        fetchPoiIds = new FetchPoiIds();
        batchUpdate = new ExecuteUpdate();
        //加载新的poiid匹配关系
        CommonsData.initNewPoiRelation(cpName);
        logger.info("Current Config= {}, cpName=[{}]", Utils.printConf(conf), cpName);
    }

    volatile boolean shutdown = false;
    private final ReentrantLock lock = new ReentrantLock();
    /*每次查询cp的数量*/
    private int queryCpCounts = conf.getInt("pageSize");
    /*update_flag 或者test_update_flag 的值*/
    private int flagValue = 1;
    /*添加任务条件*/
    private volatile int fetchCondition = proc * 3;
    /*执行批量更新条件*/
    private volatile int updateCondition =queryCpCounts * 5;
    /*批量更新参数队列最大容量*/
    private int batchUpdateQueueSize = queryCpCounts * 10;

    /*数据库操作实例*/
    private Persistence persistence = Persistence.getInstance();
    /*存放cp名称和对应的cp处理程序实例*/
//    private Map<String, AbstractSave> cpProcessors = Maps.newHashMap();

    /*任务队列，每个为多条poiid*/
    private BlockingQueue<Collection<CpInfo>> queue = new LinkedBlockingQueue<Collection<CpInfo>>(queryCpCounts * proc * 2);
    /*批量更新参数队列*/
    private BlockingQueue<Map<String, Object>> batchUpdateParams = new LinkedBlockingQueue<Map<String, Object>>(batchUpdateQueueSize);
    /*线程池， 任务处理器*/
    private final ExecutorService executor = new ThreadPoolExecutor(proc, proc, 0, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>() {
                @Override
                public boolean offer(Runnable runnable) {
                    try {
                        put(runnable);
                        return true;
                    } catch (InterruptedException e) {
                        logger.info(e.getMessage());
                    }
                    return true;
                }
            }
    );


    public void activate() {
        shutdown = false;
        fetchPoiIds.start();
        this.start();
        batchUpdate.start();
    }
    /*住线程run方法*/
    @Override
    public void run() {
        try {
            while (!shutdown) {
                if (queue.size() > 0) {
                    final Collection<CpInfo> taskPart = queue.take();
                    executor.execute(new Execution(cpProcessor, taskPart));
                    if (batchUpdateParams.size() > updateCondition) {
                        if (!batchUpdate.isInterrupted()) {
                            batchUpdate.interrupt();
                            logger.info("batch update wake up");
                        }
                    }
                    if (queue.size() < fetchCondition && !fetchPoiIds.isInterrupted()) {
                        fetchPoiIds.interrupt();
                        logger.info("fetchPoiIds wake up");
                    }
                }
            }
            executor.shutdown();
            fetchPoiIds.isInterrupted();
            batchUpdate.isInterrupted();
            logger.info("All update finished");
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*单个cp处理线程*/
    class Execution extends Thread {
        private AbstractSave processor;
        private Collection<CpInfo> taskPart;

        public Execution(AbstractSave processor, Collection<CpInfo> taskPart) {
            this.processor = processor;
            this.taskPart = taskPart;
        }

        @Override
        public void run() {
            try {
                if (taskPart.size() > 0) {
                    StopWatch watch = new Slf4JStopWatch();
                    watch.start("postInMultiThread", "process one poiid");
                    Map<String, Object> mp = processor.postInMultiThread(taskPart);
                    watch.stop();
                    if (mp != null) {
                        batchUpdateParams.put(mp);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 分页读取poiids
     */
    class FetchPoiIds extends Thread {
        private volatile int retry = 0;
        private Queue<String> poiids = new LinkedList<String>();

        public FetchPoiIds() {
            poiids.addAll(initPoiids(initPoiidCount));
        }

        @Override
        public void run() {
            while (!shutdown) {
                try {
                    if (queue.size() > fetchCondition) {
                        Thread.sleep(Integer.MAX_VALUE);
                    } else {
                        boolean fetched = putTasks();
                        if (!fetched) {
                            shutdown = true;
                            if (!batchUpdate.isInterrupted()) {
                                batchUpdate.interrupt();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    logger.debug("{}", Utils.getStackTrace(e.getStackTrace()));
                }
            }
            logger.info("fetch task finished");
        }

        public List<String> initPoiids(int counts) {
            return getPoiidFromDeep(counts);
        }

        private List<String> getPoiidFromDeep(int poiCounts) {
            List<String> _poiids = Lists.newArrayList();
            int page = (poiCounts - 1) / 1000 + 1;
            for (int i = 0; i < page; i++) {
                List<String> poiid = persistence.getPagePoiid(cpName, flagValue, i * 1000, 1000);
                _poiids.addAll(poiid);
            }
            logger.info("Initialize cp {} poiid counts={}", cpName, _poiids.size());
            return _poiids;
        }

        private List<String> getPoiidFromRti() {
            List<String> _poiids = Lists.newArrayList();
            int page = (initPoiidCount - 1) / 1000 + 1;
            for (int i = 0; i < page; i++) {
                List<String> cpId = persistence.getPageCpIds(cpName, 1, i * 1000, 1000);
                if (cpId != null && !cpId.isEmpty()) {
                    List<String> temp = persistence.getPagePoiidById(cpName, cpId);
                    _poiids.addAll(temp);
                }
            }
            return _poiids;
        }

        private boolean putTasks() {
            if (poiids.size() == 0) {
                if (retry < 6) {
                    if (retry > 3) {
                        poiids.addAll(getPoiidFromRti());
                    } else {
                        poiids.addAll(initPoiids(initPoiidCount));
                    }
                    if (poiids.size() < 1000 / 2) {
                        retry += 1;
                    }
                    logger.info("Retry to get update poiid times={}", retry);
                } else {
                    logger.info("All updated poiid processed cp is [{}] ", cpName);
                    return false;
                }
            }
            logger.info("update param[{}], condition=[{}]", batchUpdateParams.size(), updateCondition);
            int page = conf.getInt("pageSize");
            List<String> params = Lists.newArrayList();
            if (poiids.size() < page) {
                params.addAll(poiids);
                poiids.clear();
            } else {
                for (int x = 0; x < page; x++)
                    params.add(poiids.poll());
            }
            logger.info("poiid left:[{}]", poiids.size());
            if (!params.isEmpty()){
                Multimap<String, CpInfo> groupedCps = persistence.getCpFromPoiids(cpName, params);
                Collection<Collection<CpInfo>> cps = groupedCps.asMap().values();
                queue.addAll(cps);
                logger.info("Fetch cp:{}, queue size:{}", cpName, queue.size());
            }
            return true;
        }
    }

    /**
     * 批量更新flag标记
     */
    class ExecuteUpdate extends Thread {
        @Override
        public void run() {
            logger.info("Batch update process started");
            while (!shutdown) {
                try {
                    if (batchUpdateParams.size() < updateCondition) {
                        Thread.sleep(Integer.MAX_VALUE);
                    } else {
                        lock.lockInterruptibly();
                        List<Map<String, Object>> params = ImmutableList.copyOf(batchUpdateParams);
                        batchUpdateParams.clear();
                        lock.unlock();
                        logger.info("batch update list:[{}], param-size[{}]", batchUpdateParams.size(), params.size());
                        persistence.executeUpdate(params);
                    }
                } catch (Exception e) {
                    logger.debug("ExecuteUpdate Thread:{}", Utils.getStackTrace(e.getStackTrace()));
                }
            }
            List<Map<String, Object>> params = ImmutableList.copyOf(batchUpdateParams);
            batchUpdateParams.clear();
            logger.info("batch queue size", batchUpdateParams.size());
            persistence.executeUpdate(params);
            logger.info("Batch Execute Update Thread finished");
        }
    }
}
