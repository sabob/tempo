package za.sabob.tempo.util;

import java.net.*;
import java.nio.file.*;
import java.sql.*;
import javax.persistence.*;
import javax.sql.*;
import org.apache.derby.jdbc.*;

public class TestUtils {

    public static EntityManagerFactory createEntityManagerFactory() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "tempoPU" );
        return emf;
    }

    public static DataSource getDS() {

        try {
            EmbeddedConnectionPoolDataSource ds = new EmbeddedConnectionPoolDataSource();
            ds.setDatabaseName( "memory:tempo" );
            ds.setCreateDatabase( "create" );
            ds.setLoginTimeout( 1 );
            ds.setShutdownDatabase( "tempo" );
            return ds;

        } catch ( SQLException ex ) {
            throw new RuntimeException( ex );
        }

    }

    public static void createDatabase( DataSource ds ) {
        try {
            URI uri = TestUtils.class.getResource( "create-tables.sql" ).toURI();
            String ddl = new String( Files.readAllBytes( Paths.get( uri ) ) );
            System.out.println( "DDL: " + ddl );

            Connection conn = ds.getConnection();
            Statement stmnt = conn.createStatement();
            stmnt.executeUpdate( ddl );

        } catch ( Exception ex ) {
            throw new RuntimeException( ex );
        }

    }

    public static void populateDatabase( DataSource ds ) {

        Connection conn;

        try {
            conn = ds.getConnection();
            Statement stmnt = conn.createStatement();
            String DML = "INSERT INTO person(name) VALUES ('bob')";
            stmnt = conn.createStatement();
            stmnt.executeUpdate( DML );

        } catch ( SQLException ex ) {
            throw new RuntimeException( ex );
        }

    }

    public static void main( String[] args ) throws Exception {

    }
}
