package com.demo;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JacobTest {
    public static char[] markChars={',', '.', '?', '!',  ';', ':', '、', '，', '。', '？', '！', '；', '：'};

    public static void main(String[] args) {
        System.out.println("start");
        if (args.length == 1) {
            Pattern pattern = Pattern.compile("[a-z]:[/].*?");
            String path = args[0];
            path = path.replaceAll("\\\\", "/");
            path = path.toLowerCase();

            ActiveXComponent activeXComponent = null;
            Dispatch dispatch = null;

            try {
                activeXComponent = new ActiveXComponent("Sapi.SpVoice");
                dispatch = activeXComponent.getObject();
                activeXComponent.setProperty("Volume", new Variant(100));
                activeXComponent.setProperty("Rate", new Variant(0));
            } catch (Exception e) {
                e.printStackTrace();
            }

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
                        if (text.length()>100) {
//                            int partLength=text.length()/50;
//                            int count=0;
//                            while (count<text.length()) {
//                                if (count+partLength>text.length()) {
//                                    partLength=text.length()-count;
//                                }
//                                String subText=text.substring(count, count+partLength);
//                                count+=partLength;
//                                textToAudio(activeXComponent, dispatch,subText);
//                            }
                            int count=text.length()/50;
                            int position=0;
                            Map<String, Integer>param=new HashMap<>();
                            param.put("count", count);
                            param.put("position", position);
                            while (true) {
                                String subText=getSubText(text, param);
                                textToAudio(activeXComponent, dispatch,subText);
                                position=param.get("position");
                                if (position>text.length()) {
                                    System.out.println(String.format("error: position: %d, text.length(): %d", position, text.length()));
                                    break;
                                }
                                else if (position==text.length()) {
                                    break;
                                }
                            }
                        }
                        else {
                            System.out.println(text);
                            textToAudio(activeXComponent, dispatch, text);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        } else {
            System.out.println("请输入文件全路劲");
        }
        System.out.println("end");
    }

    public static String getSubText(String text, Map<String, Integer>param) {
        String subString="";
        int end=0;
        int length=text.length();
        int position=param.get("position");
        int count=param.get("count");
//        System.out.println(String.format("position: %d, count: %d, length: %d", position, count, length));
        try {
            if (position==text.length()) {
                return subString;
            }
            if (position+count>text.length()) {
                count=text.length()-position;
                param.put("count", count);
            }
            end=position+count;

            for(; end<text.length(); end++) {
                if (contains(markChars, text.charAt(end))) {
                    break;
                }
            }
            param.put("position", end);
            subString=text.substring(position, end);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subString;
    }

    private static boolean contains(char[] markChars, char c) {
        for(int i=0; i<markChars.length; i++) {
            if (markChars[i]==c) {
                return true;
            }
        }
       return false;
    }

    //只朗读
    public static void textToAudio(ActiveXComponent activeXComponent, Dispatch dispatch, String text) {
        int count=0;
        while (count<3) {
            count++;
            try {
                Dispatch.call(dispatch, "Speak", new Variant(text));
                break;
            } catch (Exception e) {
                if (count<3) {
                    System.out.println(String.format("error occur count: %d, text: %s", count, text));
                    continue;
                }
                e.printStackTrace();
                System.out.println(String.format("error: %s", text));
            }
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
