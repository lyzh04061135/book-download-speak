package com.demo;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JacobTest {
    public static void main(String[] args) {
        System.out.println("start");
        if (args.length == 1) {
            Pattern pattern = Pattern.compile("[a-z]:[/].*?");
            String path = args[0];
            path = path.replaceAll("\\\\", "/");
            path = path.toLowerCase();
            Matcher matcher = pattern.matcher(path);
            if (matcher.matches()) {
                try (FileReader fileReader = new FileReader(args[0]);
                     BufferedReader bufferedReader = new BufferedReader(fileReader)
                ) {
                    while (true) {
                        String text = bufferedReader.readLine();
                        if (text == null) {
                            break;
                        }
                        textToAudio(text);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("请输入文件全路劲");
        }
        System.out.println("end");
    }

    //只朗读
    public static void textToAudio(String text) {
        ActiveXComponent activeXComponent = null;
        Dispatch dispatch = null;
        try {
            activeXComponent = new ActiveXComponent("Sapi.SpVoice");
            dispatch = activeXComponent.getObject();
            activeXComponent.setProperty("Volume", new Variant(100));
            activeXComponent.setProperty("Rate", new Variant(-1));
            Dispatch.call(dispatch, "Speak", new Variant(text));
            dispatch.safeRelease();
            dispatch = null;
            activeXComponent.safeRelease();
            activeXComponent = null;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(String.format("error: %s", text));
        }
        try {
            if (dispatch != null) {
                dispatch.safeRelease();
            }
            if (activeXComponent != null) {
                activeXComponent.safeRelease();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //朗读后会生成文件
    public static void textToSpeech(String text) {
        ActiveXComponent ax = null;
        try {
            ax = new ActiveXComponent("Sapi.SpVoice");

            // 运行时输出语音内容
            Dispatch spVoice = ax.getObject();
            // 音量 0-100
            ax.setProperty("Volume", new Variant(100));
            // 语音朗读速度 -10 到 +10
            ax.setProperty("Rate", new Variant(-1));
            // 执行朗读
            Dispatch.call(spVoice, "Speak", new Variant(text));

            // 下面是构建文件流把生成语音文件

            ax = new ActiveXComponent("Sapi.SpFileStream");
            Dispatch spFileStream = ax.getObject();

            ax = new ActiveXComponent("Sapi.SpAudioFormat");
            Dispatch spAudioFormat = ax.getObject();

            // 设置音频流格式
            Dispatch.put(spAudioFormat, "Type", new Variant(22));
            // 设置文件输出流格式
            Dispatch.putRef(spFileStream, "Format", spAudioFormat);
            // 调用输出 文件流打开方法，创建一个.wav文件
            Dispatch.call(spFileStream, "Open", new Variant("./text.wav"), new Variant(3), new Variant(true));
            // 设置声音对象的音频输出流为输出文件对象
            Dispatch.putRef(spVoice, "AudioOutputStream", spFileStream);
            // 设置音量 0到100
            Dispatch.put(spVoice, "Volume", new Variant(100));
            // 设置朗读速度
            Dispatch.put(spVoice, "Rate", new Variant(-1));
            // 开始朗读
            Dispatch.call(spVoice, "Speak", new Variant(text));

            // 关闭输出文件
            Dispatch.call(spFileStream, "Close");
            Dispatch.putRef(spVoice, "AudioOutputStream", null);

            spAudioFormat.safeRelease();
            spFileStream.safeRelease();
            spVoice.safeRelease();
            ax.safeRelease();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
