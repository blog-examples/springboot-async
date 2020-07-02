package com.example.demo.test;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class JavaShellCmd {

    /**
     * 쉘 커멘드에서 실행시킬 프로세스에 연결할 inputStream, 그리고 그 인풋 스트림으로 데이터를 받은 컨슈머를 포함 및 정적 클래스
     * consumer로 실행할 프로세스로 부터 응답 결과를 받을것 같다.
     * Runnable을 구현한걸 보니, 별도의 스레드로 동작한다.
     * ExecutorService 라는 java 기반 스레드 관리 용 프래임 워크로 해당 클래스를 호출 할수 있다고 하는데, 해당 내용은 잘 몰르겠다.
     */
    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;
        private String psRetMsg;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
            this.psRetMsg = new String();
        }

        @Override
        public void run() {
            try {
                new BufferedReader(new InputStreamReader(inputStream,"euc-kr")).lines()
                        .forEach(consumer);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        /**
         *  이제 이부분에서 파이썬 스크립트 파일을 호출해야한다.
         *  필요한 정보
         *  0. jvm의 실행환경 OS를 파악한다.
         *  1. 파이썬 파일의 경로
         */
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");

        System.out.println("실행환경이 윈도우인가? " + isWindows);

        /**
         * 간단하게 Runtime 클래스를 사용해서 현재 사용자 디렉토리 정보를 가져와 출력하는 쉘 커멘드를 호출하는 예제
         */
        String homeDirectory = System.getProperty("user.home");
        Process process;
        if (isWindows) {
            process = Runtime.getRuntime()
                    .exec(String.format("cmd.exe /c dir %s", homeDirectory));
        } else {
            process = Runtime.getRuntime()
                    .exec(String.format("sh -c ls %s", homeDirectory));
        }
        /**
         * 쉘 프로세스를 실행하고 그 프로세스에 inputstream을 연결하고 컨슈머로 System.out::println를 넘긴다.
         */
        String psRetMsg = "";
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), (item)->{
                    System.out.println(item);
                });
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;
    }
}
