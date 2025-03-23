import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Простая реализация пула потоков в стиле Go для учебных целей.
 * Демонстрирует базовые концепции многопоточного программирования.
 */
public class GoLikeThreadPool implements CustomExecutor {
    // Логгер для отслеживания работы пула
    private static final Logger logger = Logger.getLogger(GoLikeThreadPool.class.getName());
    
    // Очередь задач (аналог канала в Go)
    private final BlockingQueue<Runnable> taskQueue;
    
    // Список рабочих потоков
    private final List<Worker> workers;
    
    // Флаг для остановки пула
    private volatile boolean isShutdown = false;

    /**
     * Создает пул потоков с указанным количеством потоков и размером очереди задач.
     * @param numberOfThreads количество рабочих потоков
     * @param queueSize размер очереди задач
     */
    public GoLikeThreadPool(int numberOfThreads, int queueSize) {
        if (numberOfThreads <= 0 || queueSize <= 0) {
            throw new IllegalArgumentException("Количество потоков и размер очереди должны быть положительными");
        }
        
        this.taskQueue = new LinkedBlockingQueue<>(queueSize);
        this.workers = new ArrayList<>();
        
        // Создаем рабочие потоки
        for (int i = 0; i < numberOfThreads; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            Thread thread = new Thread(worker, "Worker-" + (i + 1));
            thread.start();
            logger.info("Создан рабочий поток: " + thread.getName());
        }
    }

    @Override
    public void execute(Runnable task) {
        if (isShutdown) {
            throw new RejectedExecutionException("Пул потоков остановлен");
        }
        
        try {
            // Пытаемся добавить задачу в очередь
            if (!taskQueue.offer(task, 1, TimeUnit.SECONDS)) {
                // Если очередь переполнена, выполняем задачу в потоке отправителя
                logger.warning("Очередь переполнена, выполняем задачу в текущем потоке");
                task.run();
            } else {
                logger.info("Задача добавлена в очередь: " + task);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RejectedExecutionException("Не удалось добавить задачу", e);
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> futureTask = new FutureTask<>(task);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public void shutdown() {
        logger.info("Начало остановки пула потоков");
        isShutdown = true;
        workers.forEach(Worker::stop);
    }

    @Override
    public void shutdownNow() {
        logger.info("Немедленная остановка пула потоков");
        isShutdown = true;
        taskQueue.clear();
        workers.forEach(worker -> {
            worker.stop();
            worker.interrupt();
        });
    }

    /**
     * Рабочий поток, который берет задачи из очереди и выполняет их.
     */
    private class Worker implements Runnable {
        private volatile boolean running = true;
        private Thread thread;

        @Override
        public void run() {
            thread = Thread.currentThread();
            
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    // Пытаемся взять задачу из очереди
                    Runnable task = taskQueue.poll(1, TimeUnit.SECONDS);
                    if (task != null) {
                        logger.info(thread.getName() + " начал выполнение задачи");
                        task.run();
                        logger.info(thread.getName() + " завершил выполнение задачи");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.warning(thread.getName() + " ошибка при выполнении задачи: " + e.getMessage());
                }
            }
            
            logger.info(thread.getName() + " завершил работу");
        }

        void stop() {
            running = false;
        }

        void interrupt() {
            if (thread != null) {
                thread.interrupt();
            }
        }
    }
} 