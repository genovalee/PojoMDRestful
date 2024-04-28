# POJO RESTFul Api 程式撰寫、測試 
## 建立 MD 資料表

MASTER TABLE:
```sql
create table t0nj0547 
( 
  BUSSRFNO     VARCHAR2(8),           --商業統一編號  
  BUSSNM       VARCHAR2(255),         --商業名稱  
  COSTSID      VARCHAR2(2),           --公司狀態代碼  
  COSTSIDCOMT  VARCHAR2(255),         --公司狀態說明  
  REGOFC       VARCHAR2(15),          --登記機關  
  REGOFCCOMT   VARCHAR2(255),         --登記機關說明  
  BUSSLOCATION VARCHAR2(512),         --商業所在地, 
  TXDAT        DATE, 
  constraint PK_T0NJ0547 primary key (BUSSRFNO, REGOFC) 
); 
```
DETAIL TABLE
```sql
create table t0nj0547d 
( 
  BUSSRFNO  VARCHAR2(8),               --商業統一編號  
  REGOFC    VARCHAR2(255),             --登記機關  
  IT        VARCHAR2(4),               --營業項目序號  
  SALIT     VARCHAR2(10),              --營業項目代號  
  SALITCOMT VARCHAR2(2000),            --營業項目名稱 
  TXDAT     DATE, 
  constraint PK_T0NJ0547D primary key (BUSSRFNO, REGOFC, SALIT), 
  constraint FK_T0NJ0547D foreign key (BUSSRFNO, REGOFC) 
  references T0NJ0547 (BUSSRFNO, REGOFC) on delete cascade 
); 
```

## 增設Table物件，建立Java Class並宣告變數，並產生get/set method

使用jackson Annotation @JsonProperty設定json payload與宣告變數的對應關係
```java
public class T0nj0547d {
    @JsonProperty("Business_Seq_No")
    private String it;
    @JsonProperty("Business_Item")
    private String salit;
    @JsonProperty("Business_Item_Desc")
    private String salitcomt;
}
```

```java
public class T0nj0547{
    @JsonProperty("President_No")
    private String bussrfno;
    @JsonProperty("Business_Name")
    private String bussnm;
    @JsonProperty("Business_Current_Status")
    private String costsid;
    @JsonProperty("Business_Current_Status_Desc")
    private String costsidcomt;
    @JsonProperty("Business_Organization_Type_Desc")
    private String orgnTyNm;
    @JsonProperty("Agency")
    private String regofc;
    @JsonProperty("Agency_Desc")
    private String regofccomt;
    @JsonProperty("Business_Address")
    private String busslocation;
    @JsonProperty("Business_Item_Old")
    private List&lt;T0nj0547d&gt; t0nj0547d;
}
```
## 增設Java Interface作為service post 實作之用
### post使用addT0nj0547方法，get使用getT0nj0547ByBussRfnoRegoFc方法

```java
public interface T0nj0547Service {
    Response addT0nj0547(T0nj0547 t47);
    T0nj0547 getT0nj0547ByBussRfnoRegoFc(String bussrfno, String regofc);
}
```
## 增設Java Class實作Interface內宣告的類別
### Override addT0nj0547方法，定義jsonSchema內容
```java
public class T0nj0547ServiceImp implements T0nj0547Service {
    private Connection conn;
    private String resp = "";
    private String jsonSchema ="{"type":"object","required":["President_No","Business_Current_Status","Business_Current_Status_Desc","Business_Organization_Type_Desc","Agency","Agency_Desc","Business_Address"],"properties":{"President_No":{"type":"string"},"Business_Name":{"type":"string"},"Business_Current_Status":{"type":"string"},"Business_Current_Status_Desc":{"type":"string"},"Business_Organization_Type_Desc":{"type":"string"},"Agency":{"type":"string"},"Agency_Desc":{"type":"string"},"Business_Address":{"type":"string"},"Business_Item_Old":{"type":"array","items":{"type":"object","properties":{"Business_Seq_No":{"type":"string"},"Business_Item":{"type":"string"},"Business_Item_Desc":{"type":"string"}},"required":["Business_Seq_No","Business_Item","Business_Item_Desc"]}}}}";

    /**
     * @throws NamingException
     * @throws SQLException
     */
    public T0nj0547ServiceImp() throws NamingException, SQLException {
        super();
        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/XXXXDS");
        conn = ds.getConnection();
        conn.setAutoCommit(true);
    }
```
### Override addT0nj0547方法，參數為json payload並從 header取的基本認證的資料
```java
    public Response addT0nj0547(String payload, @HeaderParam("authorization") String authString) {
        try {
            if (!isUserAuthenticated(authString)) {
                resp = "{\"error\":\"User not authenticated\"}";
                return handleException(resp, Response.Status.UNAUTHORIZED);
            }

            // Load JSON Schema
            JSONObject rawSchema = new JSONObject(new JSONTokener(jsonSchema));
            Schema schema = SchemaLoader.load(rawSchema);
            // 驗證payload是否符合JSON Schema
            schema.validate(new JSONObject(payload)); // 驗證 payload 是否符合 schema 的規範。如果驗證失敗，會拋出 ValidationException。

            // 將payload轉換為T0nj0547物件
            ObjectMapper objectMapper = new ObjectMapper();
            T0nj0547 t0nj0547 = objectMapper.readValue(payload, T0nj0547.class);
            InsertDbT0nj0547 insT0nj0547 = new InsertDbT0nj0547(conn, t0nj0547);
            insT0nj0547.InsertDbT0nj0547();
            List<T0nj0547d> t0nj0547d = t0nj0547.getT0nj0547d();
            if (t0nj0547d.size() > 0) {
                InsertDbT0nj0547d insT0nj0547d =
                    new InsertDbT0nj0547d(conn, t0nj0547d, t0nj0547.getBussrfno(), t0nj0547.getRegofc());
                insT0nj0547d.InsertDbT0nj0547d();
            }
            resp = "{\"message\":\"資料已新增\",\"status\":\"" + Response.Status.CREATED + "\"}";
            return Response.status(Response.Status.CREATED)
                           .entity(resp)
                           .build();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            String resp = "{\"message\":\"" + ex.getMessage().replace("\"", "\'") + "\"}";
            return handleException(resp, Response.Status.BAD_REQUEST);
        } catch (IOException | ValidationException ex) {
            String errorMessage = "JSON Schema驗證失敗: " + ex.getMessage().trim();
            resp = "{\"error\":\"" + errorMessage + "\"}";
            return handleException(resp, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            resp = "{\"error\":\"" + e.getMessage().trim() + "\"}";
            return handleException(resp, Response.Status.BAD_REQUEST);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("連線DB發生錯誤");
                }
            }
        }
    }
```

## Override getT0nj0547ByRfnoRegoFc方法，回傳T0nj0547物件
```java
    //get url example http://localhost:7101/v0/api/get?bussrfno=01465626&regofc=123456789A
    @Override
    public T0nj0547 getT0nj0547ByBussRfnoRegoFc(String bussrfno, String regofc) {
        String sql =
            "select bussrfno, bussnm, costsid, costsidcomt, regofc, regofccomt, busslocation from t0nj0547 where bussrfno=? and regofc=?";
        String sql2 = "select bussrfno, regofc, it, salit, salitcomt from t0nj0547d where bussrfno=? and regofc=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            List<T0nj0547d> t0nj0547ds = new ArrayList<>();
            ps.setString(1, bussrfno);
            ps.setString(2, regofc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                        ps2.setString(1, rs.getString("bussrfno"));
                        ps2.setString(2, rs.getString("regofc"));
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            while (rs2.next()) {
                                T0nj0547d t0nj0547d = extractT0nj0547dFromResultSet(rs2);
                                t0nj0547ds.add(t0nj0547d);
                            }
                        }
                    }
                }
                return extractT0nj0547FromResultSet(rs, t0nj0547ds);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
        }
        return null;
    }    
```

### 增設private extractT0nj0547FromResultSet方法，return T0nj0547物件
```java
    private T0nj0547 extractT0nj0547FromResultSet(ResultSet rs, List<T0nj0547d> t0nj0547ds) throws SQLException {
        T0nj0547 t0nj0547 = new T0nj0547();
        t0nj0547.setBussrfno(rs.getString("bussrfno"));
        t0nj0547.setBussnm(rs.getString("bussnm"));
        t0nj0547.setCostsid(rs.getString("costsid"));
        t0nj0547.setCostsidcomt(rs.getString("costsidcomt"));
        t0nj0547.setRegofc(rs.getString("regofc"));
        t0nj0547.setRegofccomt(rs.getString("regofccomt"));
        t0nj0547.setBusslocation(rs.getString("busslocation"));
        t0nj0547.setT0nj0547d(t0nj0547ds);
        return t0nj0547;
    }
```

### 增設private extractT0nj0547dFromResultSet方法，return T0nj0547d物件
```java
    private T0nj0547d extractT0nj0547dFromResultSet(ResultSet rs) throws SQLException {
        T0nj0547d t0nj0547d = new T0nj0547d();
        t0nj0547d.setBussrfno(rs.getString("bussrfno"));
        t0nj0547d.setRegofc(rs.getString("regofc"));
        t0nj0547d.setIt(rs.getString("it"));
        t0nj0547d.setSalit(rs.getString("salit"));
        t0nj0547d.setSalitcomt(rs.getString("salitcomt"));
        return t0nj0547d;
    }
```
### 增設private isUserAuthenticated是否有基本認證方法，return boolean，true驗證通過，false驗證失敗
```java
    private boolean isUserAuthenticated(String authString) {
        String decodedAuth = null;
        // We need to extract data before decoding it back to original string
        String[] authParts = authString.split("\\s+");
        String authInfo = authParts[1];
        // Decode the data back to original string

        byte[] bytes = null;
        try {
            bytes = Base64.getDecoder().decode(authInfo);
            decodedAuth = new String(bytes);
        } catch (IllegalArgumentException e) {
            return false;
        } catch (Exception e) {
            return false;
        }

        StringTokenizer tokenizer = new StringTokenizer(decodedAuth, ":");
        String userName = tokenizer.nextToken();
        String password = tokenizer.nextToken();
        if ("username".equals(userName) && "XXXXXX".equals(password)) {
            return true;
        } else
            return false;
    }
```
### 增設異常處理的方法
```java
    // 異常訊息處理方法
    private Response handleException(String errorMessage, Response.Status status) {
        resp = "{\"error\":\"" + errorMessage + "\"}";
        return Response.status(status)
                       .entity(resp)
                       .build();
    }
```

## 增設兩個Insert Table的類別(InsertDbT0nj0547、InsertDbT0nj0547d)
```java
public class InsertDbT0nj0547 {
    private Connection conn = null;
    private T0nj0547 t47;

    public InsertDbT0nj0547() {
    }
    //CONSTRUCTOR
    public InsertDbT0nj0547(Connection conn, T0nj0547 t47) {
        this.conn = conn;
        this.t47 = t47;
    }

    public void InsertDbT0nj0547() throws SQLException {
        Date date = new Date(System.currentTimeMillis());
        Timestamp today = new Timestamp(date.getTime());
        String sql =
            "INSERT INTO t0nj0547(bussrfno, bussnm, costsid, costsidcomt, regofc, regofccomt, busslocation, txdat) " +
            "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t47.getBussrfno());
            ps.setString(2, t47.getBussnm());
            ps.setString(3, t47.getCostsid());
            ps.setString(4, t47.getCostsidcomt());
            ps.setString(5, t47.getRegofc());
            ps.setString(6, t47.getRegofccomt());
            ps.setString(7, t47.getBusslocation());
            ps.setTimestamp(8, today);
            ps.execute();
        }
    }
}
```

```java
public class InsertDbT0nj0547d {
    private Connection conn = null;
    private List<T0nj0547d> t47d = new ArrayList<>();
    private String bussrfno = "";
    private String regofc = "";

    public InsertDbT0nj0547d() {
    }

    public InsertDbT0nj0547d(Connection conn, List<T0nj0547d> t47d, String bussrfno, String regofc) {
        this.conn = conn;
        this.t47d = t47d;
        this.bussrfno = bussrfno;
        this.regofc = regofc;
    }

    public void InsertDbT0nj0547d() throws SQLException {
        Date date = new Date(System.currentTimeMillis());
        Timestamp today = new Timestamp(date.getTime());
        String sql = "INSERT INTO t0nj0547d(bussrfno, regofc, it, salit, salitcomt, txdat) VALUES (?,?,?,?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        for (T0nj0547d dt : t47d) {
            ps.setString(1, bussrfno);
            ps.setString(2, regofc);
            ps.setString(3, dt.getIt());
            ps.setString(4, dt.getSalit());
            ps.setString(5, dt.getSalitcomt());
            ps.setTimestamp(6, today);
            ps.execute();
        }
        ps.close();
    }
}
```
## Create Restful Service(post method)
![set post](https://github.com/genovalee/PojoMDRestful/blob/master/Demo/src/demo/entity/Image_020.png)
## Create Restful Service(get method)
![set get](https://github.com/genovalee/PojoMDRestful/blob/master/Demo/src/demo/entity/Image_022.png)
## Create Restful Service(OK)
![ok](https://github.com/genovalee/PojoMDRestful/blob/master/Demo/src/demo/entity/Image_023.png)
## 資料get測試
![get](https://github.com/genovalee/PojoMDRestful/blob/master/Demo/src/demo/entity/Image_017.png)
## 資料post測試
![post](https://github.com/genovalee/PojoMDRestful/blob/master/Demo/src/demo/entity/Image_018.png)
![query](https://github.com/genovalee/PojoMDRestful/blob/master/Demo/src/demo/entity/Image_019.png)
