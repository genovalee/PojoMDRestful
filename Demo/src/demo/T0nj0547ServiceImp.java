package demo;

import com.fasterxml.jackson.databind.ObjectMapper;

import demo.entity.T0nj0547;

import demo.entity.T0nj0547d;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import java.util.StringTokenizer;

import javax.naming.InitialContext;

import javax.naming.NamingException;

import javax.sql.DataSource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;

import org.json.JSONObject;
import org.json.JSONTokener;

/*
 *  header "Authorization" : "Basic appuser01:pDe6GE3fApcAcGbV"
 * */
@Path("/")
public class T0nj0547ServiceImp implements T0nj0547Service {
    private Connection conn;
    private String resp = "";
    private String jsonSchema =
        "{\"type\":\"object\",\"required\":[\"President_No\",\"Business_Current_Status\",\"Business_Current_Status_Desc\",\"Business_Organization_Type_Desc\",\"Agency\",\"Agency_Desc\",\"Business_Address\"],\"properties\":{\"President_No\":{\"type\":\"string\"},\"Business_Name\":{\"type\":\"string\"},\"Business_Current_Status\":{\"type\":\"string\"},\"Business_Current_Status_Desc\":{\"type\":\"string\"},\"Business_Organization_Type_Desc\":{\"type\":\"string\"},\"Agency\":{\"type\":\"string\"},\"Agency_Desc\":{\"type\":\"string\"},\"Business_Address\":{\"type\":\"string\"},\"Business_Item_Old\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"Business_Seq_No\":{\"type\":\"string\"},\"Business_Item\":{\"type\":\"string\"},\"Business_Item_Desc\":{\"type\":\"string\"}},\"required\":[\"Business_Seq_No\",\"Business_Item\",\"Business_Item_Desc\"]}}}}";

    /**
     * @throws NamingException
     * @throws SQLException
     */
    public T0nj0547ServiceImp() throws NamingException, SQLException {
        super();
        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/t078DS");
        conn = ds.getConnection();
        conn.setAutoCommit(true);
    }

    @Override
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Path("/add")
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
            String resp = "{\"message\":\"" + ex.getMessage().replace("\"", "\'") + "\"}";
            return handleException(resp, Response.Status.BAD_REQUEST);
        } catch (IOException | ValidationException ex) {
            String errorMessage = "JSON Schema驗證失敗: " + ex.getMessage().trim();
            resp = "{\"error\":\"" + errorMessage + "\"}";
            return handleException(resp, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
//            System.out.println(e.getMessage());
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

    //get url example http://localhost:7101/v0/api/get?bussrfno=01465626&regofc=123456789A
    @Override
    @GET
    @Produces("application/json")
    @Path("/get")
    public T0nj0547 getT0nj0547ByBussRfnoRegoFc(@QueryParam("bussrfno") @DefaultValue("01465626") String bussrfno,
                                                @QueryParam("regofc") @DefaultValue("123456789A") String regofc) {
        String sql =
            "select bussrfno, bussnm, costsid, costsidcomt, regofc, regofccomt, busslocation from t0nj0547 where bussrfno=? and regofc=?";
        String sql2 = "select bussrfno, regofc, it, salit, salitcomt from t0nj0547d where bussrfno=? and regofc=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            List<T0nj0547d> t0nj0547ds = new ArrayList<T0nj0547d>();
            //        System.out.println(bussrfno);
            //        System.out.println(regofc);
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

    private T0nj0547d extractT0nj0547dFromResultSet(ResultSet rs) throws SQLException {
        T0nj0547d t0nj0547d = new T0nj0547d();
        t0nj0547d.setIt(rs.getString("it"));
        t0nj0547d.setSalit(rs.getString("salit"));
        t0nj0547d.setSalitcomt(rs.getString("salitcomt"));
        return t0nj0547d;
    }

    // Header is in the format "Basic YXBwdXNlcjAxOnBEZTZHRTNmQXBjQWNHYlY="
    //header "Authorization" : "Basic appuser01:pDe6GE3fApcAcGbV"
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
        //        System.out.println(decodedAuth);

        StringTokenizer tokenizer = new StringTokenizer(decodedAuth, ":");
        String userName = tokenizer.nextToken();
        String password = tokenizer.nextToken();
        if ("appuser01".equals(userName) && "pDe6GE3fApcAcGbV".equals(password)) {
            return true;
        } else
            return false;
    }

    // 異常訊息處理方法
    private Response handleException(String errorMessage, Response.Status status) {
        resp = "{\"error\":\"" + errorMessage + "\"}";
        return Response.status(status)
                       .entity(resp)
                       .build();
    }
}
