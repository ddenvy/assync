import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * Интерфейс для пула потоков.
 * Расширяет стандартный интерфейс Executor дополнительными методами.
 */
public interface CustomExecutor extends Executor {
    /**
     * Выполняет задачу в пуле потоков.
     * @param command задача для выполнения
     */
    void execute(Runnable command);

    /**
     * Отправляет задачу, которая возвращает результат.
     * @param callable задача с возвращаемым значением
     * @return Future для получения результата
     */
    <T> Future<T> submit(Callable<T> callable);

    /**
     * Инициирует упорядоченное завершение работы пула.
     * Ранее отправленные задачи будут выполнены.
     */
    void shutdown();

    /**
     * Пытается немедленно остановить все задачи.
     * Задачи в очереди не будут выполнены.
     */
    void shutdownNow();
} 