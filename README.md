## セサミ というスマートロック
本文はなぜ日本語で書くのは一つ主な原因はSesame3は主に日本と台湾で発売されてます。とても安価で使いやすいスマートロックです。
私は初代セサミスマートロックから使い始め、なぜセサミを選んだのは  
- 日本の電気錠もあるが、スマートではない
- 日本のスマートロックは高値で、月課金のものもあった。あり得ない
- マンションお住まいのため、統一されたドアの筐体とロックの制限があった。
- 日本のGoldやMiwaのロックはなかなか欧米のスマートロックが対応されていない

その時セサミが現れました。良い商品です。初代は操作が競合したりなどの不便があったが、普通のロックに自動ロック機能や、リモートロックなどできるのは相当便利です。特に子供鍵忘れや、おばあちゃんが来るとき鍵忘れの時何回も活用しました。後やはり年が取るとドアがロックしたかしていないか。出かけた後結構悩む時があるので、これがあるとリモートでドア状態確認可能になりました。

最近セサミ3を購入し、アンロックスピードが早くなり、さらに快適です。

## セサミ WebAPIとは
初代セサミ時代からWifiモジュールが販売されて、WebAPIを利用して、ロック・アンロック可能です。いろいろ面白いインテグレーションが可能です。Googleアシスタントや、Siriを便利に使えました。

しかし、セサミ3が発売されたとともに、初代のDashboardが使えなく、初代のWebAPIも使えません。新しいAPIとなりました。またセキュリティ向上されている可能性があり、WebAPIの利用が元のような単純なものではなく、一定の暗号化処理が必要になってきました。

## セサミAndroid Appの問題
（最近自分がメインにAndroidで運用していて、IOSのことあまり気にしていませんが…）

セサミ3のAndroid Appを利用していますが、自動アンロック機能がずっと通知欄にWidgetがでばなしです。バックグラウンドで位置サービス監視が必要という裏原因も理解しますが、やはり気に入らなく、NFCタグを試しました。

NFCタグを試したところ、さらにがっかりしました。Appをオープンしないと、開錠できません。  
しかも単にあるタグの標識を記録するだけで、Appが閉じたままでタグをスキャンすると、まずシステムでApp起動の選択になります。  
自分でちょっと工夫して、NFC TagWriter by NXPを利用して、NFCTagでAppを起動するようにしたら、実際操作して見たところ、2回のスキャンが必要で、間隔時間が必要です。（さらにイライラ…）

## どうすれば一回のタッチで開錠できるでしょう

### 理想論：スマホの状態が問わず、1回スキャンしただけで、開錠する


| #      | アプローチ | メリット | デメリット |
| ----------- | ----------- | ----------- | ----------- |
| 1 | AppでWebAPIを利用して開錠操作する | 開発簡単、仕組みシンプル | Wifiモジュールが必要、スマホアンロック操作が必要 |
| 2 | AppでSesameSDKを利用して開錠操作する | Wifiモジュールが不要、仕組みシンプル | 開発難しい、スマホアンロック操作が必要 |
| 3 | スマホのNFCシミュレーター機能で、NFCTagをシミュレーションし、カードリーダー＋専用サーバで開錠操作する | スマホロックしたままで使える、自分の暗号化内容をTagに仕込めば、簡単にNFCTagを複数作り出し、子供に配布可能、公式サンプルソースをそのまま利用可能 | カードリーダーを動作させるおよびWebAPI機器が必要、ドアに設置する工夫が必要(電源どうするなど) |

#3のドアに機器の増設を考えるだけで頭いっぱいです。このオプションは本当にセサミの製造会社に考えてほしいです。スマートロックと同様、バッテリー1年持つのカードリーダー＋Wifiモジュールでできるではないかと思います。（今スマホAppのNFC機能は本当に微妙、特にAndroidのAppがいまいちです。ISOは直接開錠操作するショートカットがあるので、まだ良いかも）

自分の条件に合わせて、やはり#1のは一番現実的です。

### 現実論：スマホアンロック状態で、1回スキャンしただけで、開錠する

これをよく考えると、AppのNFC機能不要ではないか、WebAPI機能を持てば、NFCTagによる起動か、ランチャーアイコンによる起動か区別できれば、NFCTag起動時WebAPIを実行すればよいです。

## では、開発：WebAPIをJavaで利用するの難点

### 参考文献→ブログ
- 公式：https://doc.candyhouse.co/ja/SesameAPI
- https://zenn.dev/key3/articles/6c1c2841d7a8a2
- https://github.com/mochipon/sesame-qr-reader/blob/main/pages/index.vue#L56-L137

### 文献で書いたものを重複せず、実際あった問題と解決コードを載せる

- Nodejsではなく、Javaの場合、URLDecodeがとても重要です。最初はこちらに嵌めて、Base64のデコードがうまくいかず…  
   下のscanStrは文献・ブログに記載があったSesameの共有QRコードからスキャンした文字列です。  
   下記コードはQRコードから読み取った情報（設備名、UUID、秘密キーを含めて）を解読しました。
   そのため、私のコードを利用する際、秘密キーを気にする必要がなく、QRコード情報＋APIKeyでOKです。
   ```java
    private void init() {
        String[] paramParts = getParamParts(this.scanStr);
        HashMap<String, String> paramMap = getParamMap(paramParts);
        this.dataType = paramMap.get("t");
        this.permission = paramMap.get("l");
        this.name = paramMap.get("n");
        String sk = paramMap.get("sk");
        byte[] skBytes = Base64.getDecoder().decode(sk);
        this.deviceType = bytesToHex(Arrays.copyOfRange(skBytes, 0,1));
        this.secretKey = bytesToHex(Arrays.copyOfRange(skBytes, 1,17));
        this.publicKey = bytesToHex(Arrays.copyOfRange(skBytes, 17,81));
        this.keyIndex = bytesToHex(Arrays.copyOfRange(skBytes, 81,83));
        this.uuid = bytesToHex(Arrays.copyOfRange(skBytes, 83,99));
    }

    private HashMap<String, String> getParamMap(String[] paramParts) {
        HashMap<String, String> paramMap = new HashMap<>();
        for (String paramPart : paramParts) {
            String[] keyVal = paramPart.split("=");
            if (keyVal.length != 2) {
                throw new RuntimeException("QR code information is not correct!");
            }
            paramMap.put(keyVal[0], keyVal[1]);
        }
        return paramMap;
    }

    private String[] getParamParts(String uri) {
        String[] uriParts = uri.split("\\?");
        if (uriParts.length < 2) {
            throw new RuntimeException("QR code uri information is not correct!");
        }
        try {
            String paramUrl = java.net.URLDecoder.decode(uriParts[1], StandardCharsets.UTF_8.name());
            return paramUrl.split("&");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("QR code uri information is not correct!");
        }
    }

    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
   ```
- generateRandomTagのロジックのJava書き換え：正確にByte切り出し部分の理解とCMAC暗号化の実装  
   ここは難しいから、一々Nodejsの実行内容をデバッグで比べながら進めました。結果的に動けましたので、それ以上語る内容はございません(^_-)-☆。
   ```java
    public String generateRandomTag(){
        // 1. timestamp  (SECONDS SINCE JAN 01 1970. (UTC))  // 1621854456905
        long timestamp = new Date().getTime() / 1000;
        // 2. timestamp to uint32  (little endian)   //f888ab60
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(timestamp);
        // 3. remove most-significant byte    //0x88ab60
        byte[] message = Arrays.copyOfRange(buffer.array(), 1,4);
        return getCMAC(parseHexStr2Byte(secretKey), message).replace(" ","");
    }

    private byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length()/2];
        for (int i = 0; i < hexStr.length()/2; i++) {
            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
            result[i] = (byte)(high * 16 + low);
        }
        return result;
    }

    public String getCMAC(byte[] secretKey, byte[] msg) {
        CipherParameters params = new KeyParameter(secretKey);
        BlockCipher aes = new AESEngine();
        CMac mac = new CMac(aes);
        mac.init(params);
        mac.update(msg, 0, msg.length);
        byte[] out = new byte[mac.getMacSize()];
        mac.doFinal(out, 0);

        StringBuilder s19 = new StringBuilder();
        for (byte b : out) {
            s19.append(String.format("%02X ", b));
        }
        return s19.toString();
    }
   ```
   
- Rest APIリクエストの実装（できるだけ小さいAppサイズにしたいため、サードパーティJar利用せず）
   これは多くの人が調べればかけるもので、一応ソリューションとして記載します。generateRandomTagや、Historyなどの値設定方法を示します。
   ```java
    private int executeCmd(String cmdStr) {
        String base64History = Base64.getEncoder().encodeToString("NFC Unlock".getBytes());
        String sign = generateRandomTag();
        String json = String.format("{\"cmd\": \"%s\", \"history\": \"%s\",\"sign\": \"%s\"}", cmdStr, base64History, sign);
        try {
            URL url = new URL("https://app.candyhouse.co/api/sesame2/" + this.deviceId + "/cmd");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("x-api-key", apiKey);
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
            os.close();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
            br.close();
            int code = con.getResponseCode();
            con.disconnect();
            return code;
        } catch (IOException e) {
            e.printStackTrace();
            return 400;
        }
    }
   ```


- その他：Android／HarmonyOS実装で、UI Threadとのやり取りの非同期実装
   UI Threadと時間がかかるネットワーク処理の同期・非同期処理が一番理解に時間が掛かりました。自分は初心者ですが、下記は自分的の答えです。同期Executeと非同期Executeを両方用意し、非同期の場合、Callbackを用意しています。同期処理はメインThreadでTimeout待機し、WebAPIの応答コードを返すことです。   
   ```java
    public class SesameCmd implements Runnable, Callback {
        public SesameCmd(
                QRCodeInfo info,
                String apiKey,
                String cmd) {...}

        @Override
        public void run() {
            int code = executeCmd(cmd);
            if (c != null) {
                this.c.callback(code);
            }
        }

        public void executeCmdAsynchronously(Callback c) {
            this.c = c;
            new Thread(this).start();
        }

        public int executeCmdSynchronously() {
            this.c = this;
            int timeout = 100000;
            new Thread(this).start();
            try {
                while (responseCode == 0 && timeout > 0) {
                    Thread.sleep(1000);
                    timeout = timeout - 1000;
                }
                return responseCode;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return responseCode;
            } finally {
                this.c = null;
                this.responseCode = 0;
            }
        }

        @Override
        public void callback(int code) {
            this.responseCode = code;
        }

        private int executeCmd(String cmdStr) {...}

        public String generateRandomTag(){...}

        private byte[] parseHexStr2Byte(String hexStr) {...}

        public String getCMAC(byte[] secretKey, byte[] msg) {...}
    }
   ```

## 結局Appで何を実現したか…
- QRコードをスキャンして、直接すべての情報を保存します。
- APIKeyはユーザご自身で取得し、入力してテストする機能、テストで成功すると、自動的に保存されます。
- アンロック・ロック操作すると、結果画面が表示され、2秒後自動的にApp終了します。
- ランチャーアイコンで入ると、設定画面、NFCで起動する場合設備情報が十分であれば、スマートロック動作させる、でなければ設定画面へ
- 設定完了後、NFC TagWriter by NXPでApp起動Tag書き込みで、com.giko.sesamenfcを書き込めば、NFCTag作成完了。このTagで動作させることが可能です。

![picture](https://yougikou.github.io/img/post/2021-10-31-SesameSmartlockCustomSolution_1.jpg)


一応QRコードがある＋APIKeyがある前提のため、セキュリティ的にAppは一般ユーザでも使える状態になります。
また裏で変な情報別のサーバに送信しているかいないか怪しい疑われるかものため、
全量ソースは下記に公開しています。利用するオープンソースはあるが、みんなGoogleなど実績があるものになります。
https://github.com/yougikou/NFC4Sesame  

少し残念ですが、自分はMate30Proを使っているので、HarmonyOSの開発試しで、このAppを作成しました。
Androidで直接動作できないが、Javaサンプルがあれば、組み込みは簡単だと思います。  
これがあれば、Androidのスマートウォッチや、HarmonyOSの軽量スマートウォッチ（GT2 Proなど）でも操作可能になります。
（次にチャレンジしてみます）

## 動作してみるイメージは下記のようです。
2回スキャンのイライラ感が軽減ですます。

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/zqYlZ_rHLwM/0.jpg)](https://www.youtube.com/watch?v=zqYlZ_rHLwM)

著者権を気にしていて、アイコンも自分で作りました。

## まとめ
セサミのAppにも上記アドバイスをしましたが、うまく解決してくれるかどうか微妙です。
日本はやはりiPhone大軍がメジャーかも。
でもこのぐらいの努力で、Androidユーザ・HarmonyOSユーザが少しでも良い製品を生活で便利に使えたら、嬉しいことだと思います。
後ほど日本語化をもっと完璧にやろうかと思います。今は中国語でハードコーディングしています。

後続スマートウォッチで操作可能にしたいが、いつもデカいスマホを取り出すのは不便です…
セサミの公式開発はまずやってくれることを期待したいが、我慢できなければやってしまうかも。

興味・質問があったら、コメント残してね
