package com.huaban.analysis.jieba;

import com.huaban.analysis.jieba.config.PropertyUtil;

import org.elasticsearch.SpecialPermission;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 字典加载类，加载顺序：1、加载系统词库； 2、加载用户词库（以.dict结尾，支持conf目录下多个）
 */
public class WordDictionary {
    private static WordDictionary singleton;
    private static final String MAIN_DICT = "dict.txt"; //系统词库文件
    private static final String DICT_STOP_WORDS = "stop_words.txt"; //停止词文件
    private static final String DICT_IDF = "idf_dict.txt"; //IDF逆文件词语料库
    private static final String DICT_USER_SUFFIX = ".dict"; //用户词库文件后缀

    private static HashMap<String, Double> idfMap; //IDF逆文件语料库
    private static HashSet<String> stopWordsSet; //停止词
    private static double idfMedian; //idf中位数

    private final Map<String, Double> freqs = new HashMap<String, Double>();
    private final Set<String> loadedPath = new HashSet<String>();

    private Double minFreq = Double.MAX_VALUE;
    private Double total = 0.0;
    private DictSegment _dict;
    private Path configPath;

    private WordDictionary() {
        this.configPath = Paths.get(PropertyUtil.getKey("config"));

        SpecialPermission.check();
        AccessController.doPrivileged((PrivilegedAction<WordDictionary>) () -> {
            System.out.println(String.format(Locale.getDefault(), "插件配置文件路径为：%s",
                configPath.toAbsolutePath().toString()));
            try {
                this.loadDict(); //加载系统词典
                if (stopWordsSet == null) {
                    System.out.println(String.format(Locale.getDefault(), "加载停止词词典[%s]成功",
                        configPath.resolve(DICT_STOP_WORDS).toAbsolutePath().toString()));
                    stopWordsSet = new HashSet<>();
                    loadStopWords(stopWordsSet, new FileInputStream(configPath.resolve(DICT_STOP_WORDS).toAbsolutePath().toFile()));
                }
                if (idfMap == null) {
                    System.out.println(String.format(Locale.getDefault(), "加载IDF语料库[%s]成功",
                        configPath.resolve(DICT_IDF).toAbsolutePath().toString()));
                    idfMap = new HashMap<>();
                    loadIDFMap(idfMap, new FileInputStream(configPath.resolve(DICT_IDF).toAbsolutePath().toFile()));
                }
                //加载用户自定义词典
                init(configPath);

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            return this;
        });
    }

    public static WordDictionary getInstance() {
        if (singleton == null) {
            synchronized (WordDictionary.class) {
                if (singleton == null) {
                    singleton = new WordDictionary();
                    return singleton;
                }
            }
        }
        return singleton;
    }

    /**
     * for ES to initialize the user dictionary.
     *
     * @param configFile
     */
    public void init(Path configFile) {
        String abspath = configFile.toAbsolutePath().toString();
        System.out.println("开始加载用户自定义词典，目录:" + abspath);
        synchronized (WordDictionary.class) {
            if (loadedPath.contains(abspath)) {
                return;
            }
            DirectoryStream<Path> stream;
            try {
                stream = Files.newDirectoryStream(configFile, String.format(Locale.getDefault(), "*%s", DICT_USER_SUFFIX));
                for (Path path : stream) {
                    System.out.println(String.format(Locale.getDefault(), "加载用户自定义词典[%s]", path.toString()));
                    loadUserDict(path);
                }
                loadedPath.add(abspath);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(String.format(Locale.getDefault(), "%s: load user dict failure!", configFile.toString()));
            }
        }
    }

    public void init(String[] paths) {
        synchronized (WordDictionary.class) {
            for (String path : paths) {
                if (!loadedPath.contains(path)) {
                    try {
                        System.out.println("initialize user dictionary: " + path);
                        loadUserDict(path);
                        loadedPath.add(path);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        System.err.println(String.format(Locale.getDefault(), "%s: load user dict failure!", path));
                    }
                }
            }
        }
    }

    /**
     * let user just use their own dict instead of the default dict
     */
    public void resetDict() {
        _dict = new DictSegment((char) 0);
        freqs.clear();
    }

    /**
     * 加载默认系统词典
     */
    public void loadDict() throws FileNotFoundException {
        _dict = new DictSegment((char) 0);
        InputStream is = new FileInputStream(configPath.resolve(MAIN_DICT).toAbsolutePath().toFile());
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            long s = System.currentTimeMillis();
            while (br.ready()) {
                String line = br.readLine();
                String[] tokens = line.split("[\t ]+");
                // if (tokens.length < 2) {
                // continue; // 词典格式和 dict.txt 一样，一个词占一行；每一行分三部分：词语、词频（可省略）、词性（可省略）
                //下方做了一个兼容处理
                // }
                String word = tokens[0];
                double freq = Double.valueOf(tokens.length < 2 ? "3" : tokens[1]);
                total += freq;
                word = addWord(word);
                freqs.put(word, freq);
            }
            // normalize
            for (Entry<String, Double> entry : freqs.entrySet()) {
                entry.setValue((Math.log(entry.getValue() / total)));
                minFreq = Math.min(entry.getValue(), minFreq);
            }
            System.out.println(String.format(Locale.getDefault(), "加载系统词典[%s] load finished, time elapsed %d ms",
                configPath.resolve(MAIN_DICT).toAbsolutePath().toString(), System.currentTimeMillis() - s));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(String.format(Locale.getDefault(), "%s load failure!", MAIN_DICT));
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (IOException e) {
                System.err.println(String.format(Locale.getDefault(), "%s close failure!", MAIN_DICT));
            }
        }
    }


    public String addWord(String word) {
        if (null != word && !"".equals(word.trim())) {
            String key = word.trim().toLowerCase(Locale.getDefault());
            _dict.fillSegment(key.toCharArray());
            return key;
        } else {
            return null;
        }
    }


    public void loadUserDict(Path userDict) {
        loadUserDict(userDict, StandardCharsets.UTF_8);
    }

    public void loadUserDict(String userDictPath) {
        loadUserDict(userDictPath, StandardCharsets.UTF_8);
    }

    public void loadUserDict(Path userDict, Charset charset) {
        try {
            BufferedReader br = Files.newBufferedReader(userDict, charset);
            long s = System.currentTimeMillis();
            int count = 0;
            while (br.ready()) {
                String line = br.readLine();
                String[] tokens = line.split("[\t ]+");

                if (tokens.length < 1) {
                    // Ignore empty line
                    continue;
                }

                String word = tokens[0];

                double freq = 3.0d;
                if (tokens.length == 2) {
                    freq = Double.valueOf(tokens[1]);
                }
                word = addWord(word);
                freqs.put(word, Math.log(freq / total));
                count++;
            }
            System.out.println(String.format(Locale.getDefault(), "user dict %s load finished, tot words:%d, time elapsed:%dms", userDict.toString(), count, System.currentTimeMillis() - s));
            br.close();
        } catch (IOException e) {
            System.err.println(String.format(Locale.getDefault(), "%s: load user dict failure!", userDict.toString()));
        }
    }

    public void loadUserDict(String userDictPath, Charset charset) {
        InputStream is = this.getClass().getResourceAsStream(userDictPath);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, charset));

            long s = System.currentTimeMillis();
            int count = 0;
            while (br.ready()) {
                String line = br.readLine();
                String[] tokens = line.split("[\t ]+");

                if (tokens.length < 1) {
                    // Ignore empty line
                    continue;
                }

                String word = tokens[0];

                double freq = 3.0d;
                if (tokens.length == 2) {
                    freq = Double.valueOf(tokens[1]);
                }
                word = addWord(word);
                freqs.put(word, Math.log(freq / total));
                count++;
            }
            System.out.println(String.format(Locale.getDefault(), "user dict %s load finished, tot words:%d, time elapsed:%dms", userDictPath, count, System.currentTimeMillis() - s));
            br.close();
        } catch (IOException e) {
            System.err.println(String.format(Locale.getDefault(), "%s: load user dict failure!", userDictPath));
        }
    }

    /**
     * 默认jieba分词的停词表
     * url:https://github.com/yanyiwu/nodejieba/blob/master/dict/stop_words.utf8
     * @param set
     * @param in filePath
     */
    private void loadStopWords(Set<String> set, InputStream in) {
        BufferedReader bufr;
        try {
            bufr = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = bufr.readLine()) != null) {
                set.add(line.trim());
            }
            try {
                bufr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * idf值本来需要语料库来自己按照公式进行计算，不过jieba分词已经提供了一份很好的idf字典，所以默认直接使用jieba分词的idf字典
     * url:https://raw.githubusercontent.com/yanyiwu/nodejieba/master/dict/idf.utf8
     * @param map
     * @param in
     */
    private void loadIDFMap(Map<String, Double> map, InputStream in) {
        BufferedReader bufr;
        try {
            bufr = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = bufr.readLine()) != null) {
                String[] kv = line.trim().split(" ");
                map.put(kv[0], Double.parseDouble(kv[1]));
            }
            try {
                bufr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 计算idf值的中位数
            List<Double> idfList = new ArrayList<>(map.values());
            Collections.sort(idfList);
            idfMedian = idfList.get(idfList.size() / 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Double> getIdfMap() {
        return idfMap;
    }

    public HashSet<String> getStopWordsSet() {
        return stopWordsSet;
    }

    public double getIdfMedian() {
        return idfMedian;
    }

    public DictSegment getTrie() {
        return this._dict;
    }


    public boolean containsWord(String word) {
        return freqs.containsKey(word);
    }


    public Double getFreq(String key) {
        if (containsWord(key)) {
            return freqs.get(key);
        } else {
            return minFreq;
        }
    }
}
