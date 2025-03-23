/**
 * Демонстрационная программа для пула потоков.
 * Показывает основные возможности и принципы работы.
 */
public class Main {
    public static void main(String[] args) {
        // Создаем пул с 2 потоками и очередью на 10 задач
        GoLikeThreadPool pool = new GoLikeThreadPool(2, 10);
        
        System.out.println("=== Демонстрация 1: Простые задачи ===");
        // Запускаем 5 простых задач
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            pool.execute(() -> {
                System.out.println("Задача " + taskId + " начала выполнение");
                try {
                    // Имитируем работу
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Задача " + taskId + " завершена");
            });
        }
        
        // Ждем завершения всех задач
        sleep(6000);
        
        System.out.println("\n=== Демонстрация 2: Задачи с возвращаемым значением ===");
        // Запускаем задачу, которая возвращает результат
        var future = pool.submit(() -> {
            System.out.println("Вычисляем результат...");
            Thread.sleep(1000);
            return 42;
        });
        
        try {
            int result = future.get();
            System.out.println("Получен результат: " + result);
        } catch (Exception e) {
            System.out.println("Ошибка при получении результата: " + e.getMessage());
        }
        
        // Завершаем работу пула
        System.out.println("\nЗавершаем работу пула потоков...");
        pool.shutdown();
    }
    
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 