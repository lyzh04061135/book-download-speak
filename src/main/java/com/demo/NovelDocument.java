package com.demo;

import com.demo.utils.CommonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.*;

@Data
public class NovelDocument {
    private String url;
    private String chapterRule;
    private String contentRule;
    private String path;
    private String charset;

    public NovelDocument(String url, String chapterRule, String contentRule, String path, String charset) {
        this.url = url;
        this.chapterRule = chapterRule;
        this.contentRule = contentRule;
        this.path = path;
        this.charset = charset;
    }

    public boolean download() {
        if (CommonUtil.isBlank(url)) {
            System.out.println("url is empty!");
            return false;
        }
        if (CommonUtil.isBlank(path)) {
            System.out.println("path is empty!");
            return false;
        }

        getDefaultRule();

        if (CommonUtil.isBlank(chapterRule)) {
            System.out.println("url is empty!");
            return false;
        }
        if (CommonUtil.isBlank(contentRule)) {
            System.out.println("contentRule is empty!");
            return false;
        }

        if (CommonUtil.isBlank(charset)) {
            charset = "utf-8";
        }

        boolean flag = true;

        //download chapter list
        String html = null;
        try {
            html = download(url);
            if (CommonUtil.isBlank(html)) {
                System.out.println(url);
                return false;
            }
        } catch (Exception e) {
            System.out.println(url);
            e.printStackTrace();
            return false;
        }

        //download chapters
        Document document = Jsoup.parse(html);
        Elements elements = document.select(chapterRule);
        List<List<String>> list = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < elements.size(); i++) {
            try {
                Element element = elements.get(i);
                String chapterUrl = getChapterUrl(element);
                String chapterTile = element.text();
                if (CommonUtil.isBlank(chapterUrl)) {
                    System.out.println(String.format("warn chapter %d", i));
                } else {
                    if (chapterTile == null) {
                        chapterTile = "";
                    }
                    List<String> urlAndTile = new ArrayList<>();
                    urlAndTile.add(chapterUrl);
                    urlAndTile.add(chapterTile);
                    if (map.get(chapterUrl) == null) {
                        map.put(chapterUrl, chapterTile);
                    } else {
                        for (int j = 0; j < list.size(); j++) {
                            if (list.get(j).get(0).equals(chapterUrl)) {
                                list.remove(j);
                                break;
                            }
                        }
                    }
                    list.add(urlAndTile);
                }
            } catch (Exception e) {
                System.out.println(String.format("element i: %d fail!", i));
                e.printStackTrace();
            }
        }

        BufferedWriter bufferedWriter = null;

        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
        } catch (Exception e) {
            System.out.println(String.format("open file %s fail!", path));
            e.printStackTrace();
            return false;
        }

        Iterator<List<String>> iterator = list.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            count++;
            List<String> subList = iterator.next();
            System.out.println(String.format("%s, %s", subList.get(0), subList.get(1)));
            if (count < TestForm.from) {
                iterator.remove();
            } else if (count > TestForm.to) {
                iterator.remove();
            }
        }

        System.out.println("-------------------------------");

        for (int i = 0; i < list.size(); i++) {
            System.out.println(String.format("%s, %s", list.get(i).get(0), list.get(i).get(1)));
            try {
                html = download(list.get(i).get(0));
                if (CommonUtil.isBlank(html)) {
                    System.out.println(String.format("warn %s, %s", list.get(i).get(0), list.get(i).get(1)));
                    continue;
                }
                document = Jsoup.parse(html);
                Element element = document.selectFirst(contentRule);
                String content = element.text();
                if (charset.toLowerCase().equals("utf-8") || charset.toLowerCase().equals("utf8")) {
                    bufferedWriter.write(list.get(i).get(1));
                    bufferedWriter.write("\r\n");
                    bufferedWriter.write(content);
                } else {
                    bufferedWriter.write(new String(list.get(i).get(1).getBytes(charset), "utf-8"));
                    bufferedWriter.write("\r\n");
                    bufferedWriter.write(new String(content.getBytes(charset), "utf-8"));
                }
                bufferedWriter.write("\r\n");
                bufferedWriter.flush();
            } catch (Exception e) {
                System.out.println(String.format("chapter url: %s, chapter title: %s", list.get(i).get(0), list.get(i).get(1)));
                e.printStackTrace();
            }
        }

        try {
            bufferedWriter.close();
        } catch (Exception e) {
            System.out.println(String.format("close file %s fail!", path));
            e.printStackTrace();
            return false;
        }

        return flag;
    }

    private void getDefaultRule() {
        if (CommonUtil.isBlank(chapterRule)) {
            if (url.startsWith("https://www.biqukan.com")) {
                chapterRule = "body > div.listmain > dl > dd";
            }
        }

        if (CommonUtil.isBlank(contentRule)) {
            if (url.startsWith("https://www.biqukan.com")) {
                contentRule = "#content";
            }
        }
    }

    private String getChapterUrl(Element element) throws Exception {
        URI uri = new URI(url);
        Element a = element.selectFirst("a");
        String url = a.attr("href");
        uri = uri.resolve(url);
        url = uri.toURL().toString();
        return url;
    }

    private String download(String url) throws Exception {
        String html = null;
        for (int i = 0; i < 3; i++) {
            try {
                Map<String, String> headers = new HashMap<>();
                headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
                headers.put("Hm_lvt_d980a3f9499907d0586dbac4f3207804", "" + new Date().getTime());
                Connection connection = Jsoup.connect(url).timeout(5000).headers(headers);
                Thread.sleep(300);
                Document document = connection.get();
                html = document.outerHtml();
                if (!CommonUtil.isBlank(html)) {
                    break;
                } else {
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                System.out.println(url);
                e.printStackTrace();
                Thread.sleep(10000);
            }
        }
        return html;
    }

    public static void main(String[] args) throws Exception {
        String url="https://www.cnblogs.com/softidea/p/7101091.html";
        String trCssQuery="#article_content > table > tbody > tr";
        int trFrom=1;
        String tdCssQuery="td";
        int tdKey=1;
        int tdValue=2;

        Map<String, String>map=new LinkedHashMap<>();

        Connection connection=Jsoup.connect(url).timeout(5*60*1000);
        Document document=connection.get();
        Elements trElements=document.select(trCssQuery);
        for(int i=trFrom; i<trElements.size(); i++) {
            Element trElement=trElements.get(i);
            Elements tdElements=trElement.select(tdCssQuery);
            String key=tdElements.get(tdKey).text().trim();
            if (CommonUtil.isBlank(key)) {
                System.out.println(trElement.text());
                continue;
            }
            key=key.trim();
            if (!CommonUtil.isEnglish(key)) {
                continue;
            }
            int index =key.indexOf(" ");
            if (index>0) {
                key=key.substring(index+1);
            }
            index=key.indexOf(" ");
            if (index>0) {
                key=key.substring(index+1);
            }
            index=key.indexOf(" ");
            if (index>0) {
                key=key.substring(index+1);
            }
            index=key.lastIndexOf(".");
            if (index>0) {
                key=key.substring(index+1);
            }
            String value=tdElements.get(tdValue).text();
            value=value.trim();
            if (value.equals("N/A")) {
                System.out.println(trElement.text());
                continue;
            }
            value=CommonUtil.wrapper(value);
//            index=value.lastIndexOf(".");
//            if (index>0) {
//                value=value.substring(index+1);
//            }
            map.put(key, value);
        }
        ObjectMapper objectMapper=new ObjectMapper();
        String jsonString=objectMapper.writeValueAsString(map);
        System.out.println(jsonString);
    }

}
