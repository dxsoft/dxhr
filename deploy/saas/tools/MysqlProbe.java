import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * JDBC helper for SaaS smoke / provisioning.
 * Args: password [sql...]
 *   or: password --file path/to.sql databaseName
 */
public class MysqlProbe {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: MysqlProbe <password> [sql...]");
            System.err.println("   or: MysqlProbe <password> --file <schema.sql> <database>");
            System.exit(2);
        }
        String password = args[0];
        if (args.length >= 4 && "--file".equals(args[1])) {
            String file = args[2];
            String database = args[3];
            String url = "jdbc:mysql://127.0.0.1:3306/" + database
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
                    + "&allowMultiQueries=true&characterEncoding=utf8";
            String sql = Files.readString(Path.of(file), StandardCharsets.UTF_8);
            sql = sql.replace("utf8mb4_0900_ai_ci", "utf8mb4_unicode_ci");
            sql = sql.replace("utf8mb4_0900_as_ci", "utf8mb4_unicode_ci");
            try (Connection c = DriverManager.getConnection(url, "root", password);
                 Statement st = c.createStatement()) {
                st.execute(sql);
                System.out.println("LOADED:" + file + " -> " + database);
            }
            return;
        }
        String url = "jdbc:mysql://127.0.0.1:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        try (Connection c = DriverManager.getConnection(url, "root", password)) {
            if (args.length == 1) {
                System.out.println("OK");
                return;
            }
            try (Statement st = c.createStatement()) {
                for (int i = 1; i < args.length; i++) {
                    st.execute(args[i]);
                    System.out.println("EXEC:" + args[i]);
                }
            }
        }
    }
}
