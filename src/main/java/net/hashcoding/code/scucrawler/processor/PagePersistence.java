package net.hashcoding.code.scucrawler.processor;

import net.hashcoding.code.scucrawler.entity.Page;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PagePersistence {
    private volatile static PagePersistenceImpl instance;

    private static PagePersistenceImpl getInstance() {
        if (instance == null) synchronized (PagePersistence.class) {
            if (instance == null)
                instance = new PagePersistenceImpl();
        }
        return instance;
    }

    public static int count() {
        return getInstance().count();
    }

    public static void submit(Page page) {
        getInstance().submit(page);
    }

    public static void exit() {
        getInstance().exit();
    }

    public static void exitAsync() {
        getInstance().exitAsync();
    }

    private static class PagePersistenceImpl {
        private AtomicInteger counter = new AtomicInteger(0);
        private AtomicBoolean exit = new AtomicBoolean(false);
        private ThreadPoolExecutor executor =
                new ThreadPoolExecutor(
                        5,
                        9,
                        3,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>());

        private int count() {
            return counter.get();
        }

        private void submit(Page page) {
            counter.incrementAndGet();
            executor.execute(Submitter.create(page));
        }

        private void exit() {
            exitAsync();
            while (!executor.isTerminated()) {
                try {
                    TimeUnit.MICROSECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void exitAsync() {
            exit.set(true);
            executor.shutdown();
        }
    }

    private static class Submitter implements Runnable {
        private Page page;

        static Submitter create(Page page) {
            return new Submitter(page);
        }

        private Submitter(Page page) {
            this.page = page;
        }

        @Override
        public void run() {
            System.out.println("title: " + page.title);
        }
    }
}