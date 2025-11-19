package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;
import java.io.Console;
import java.util.Arrays;

public class PasswordUtil {

    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(12));
    }

    public static boolean verify(String plain, String hashed) {
        return BCrypt.checkpw(plain, hashed);
    }

    /**
     * Minimal helper: run this class to generate a bcrypt hash for a plain password.
     *
     * Usage:
     *  - From IDE: run PasswordUtil.main(new String[] {"yourNewPassword"});
     *  - From command line: java -cp <your-classpath> edu.univ.erp.auth.PasswordUtil yourNewPassword
     *  - If no arg is provided and a Console is available, it will prompt you (hidden input).
     *
     * After this prints the hash, copy it and update your users_auth row:
     * UPDATE erp_auth.users_auth
     * SET password_hash = '<PASTED_HASH>'
     * WHERE user_id = 'a2d9fca8-c495-11f0-bab8-246a0e189e90';
     */
    public static void main(String[] args) {
        String plain;
        Console console = System.console();

        if (args != null && args.length > 0 && args[0] != null && args[0].length() > 0) {
            // join all args so passwords with spaces are supported
            plain = String.join(" ", Arrays.asList(args)).trim();
        } else if (console != null) {
            char[] pwdChars = console.readPassword("Enter new password: ");
            if (pwdChars == null) {
                System.err.println("No password entered. Exiting.");
                return;
            }
            plain = new String(pwdChars);
            // clear sensitive char array
            Arrays.fill(pwdChars, '\0');
        } else {
            System.err.println("Provide password as program argument or run from an environment with a Console.");
            System.err.println("Example: java -cp <classpath> edu.univ.erp.auth.PasswordUtil myNewPass123");
            return;
        }

        if (plain.isEmpty()) {
            System.err.println("Empty password not allowed. Exiting.");
            return;
        }

        String hashed = hash(plain);
        System.out.println("BCrypt hash (copy this into DB):");
        System.out.println(hashed);
        System.out.println();
        System.out.println("SQL to run (replace the hash and user_id as needed):");
        System.out.println("UPDATE erp_auth.users_auth");
        System.out.println("SET password_hash = '" + hashed + "'");
        System.out.println("WHERE user_id = 'a2d9fca8-c495-11f0-bab8-246a0e189e90';");
    }
}
