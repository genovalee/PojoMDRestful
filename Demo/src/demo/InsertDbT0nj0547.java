package demo;

import demo.entity.T0nj0547;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

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
