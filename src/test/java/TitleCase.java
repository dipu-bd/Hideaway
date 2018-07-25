/*
 * Copyright (C) 2018 Sudipto Chandra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Convert a string to title case in java (with tests).
 *
 * @author Sudipto Chandra
 */
public abstract class TitleCase {

    /**
     * Returns the character type. <br>
     * <br>
     * Digit = 2 <br>
     * Lower case alphabet = 0 <br>
     * Uppercase case alphabet = 1 <br>
     * All else = -1.
     *
     * @param ch
     * @return
     */
    private static int getCharType(char ch) {
        if (Character.isLowerCase(ch)) {
            return 0;
        } else if (Character.isUpperCase(ch)) {
            return 1;
        } else if (Character.isDigit(ch)) {
            return 2;
        }
        return -1;
    }

    /**
     * Converts any given string in camel or snake case to title case.
     * <br>
     * It uses the method getCharType and ignore any character that falls in
     * negative character type category. It separates two alphabets of not-equal
     * cases with a space. It accepts numbers and append it to the currently
     * running group, and puts a space at the end.
     * <br>
     * If the result is empty after the operations, original string is returned.
     *
     * @param text the text to be converted.
     * @return a title cased string
     */
    public static String titleCase(String text) {
        if (text == null || text.length() == 0) {
            return text;
        }

        char[] str = text.toCharArray();
        StringBuilder sb = new StringBuilder();

        boolean capRepeated = false;
        for (int i = 0, prev = -1, next; i < str.length; ++i, prev = next) {
            next = getCharType(str[i]);
            // trace consecutive capital cases
            if (prev == 1 && next == 1) {
                capRepeated = true;
            } else if (next != 0) {
                capRepeated = false;
            }
            // next is ignorable
            if (next == -1) {
                // System.out.printf("case 0, %d %d %s\n", prev, next, sb.toString());
                continue; // does not append anything
            }
            // prev and next are of same type
            if (prev == next) {
                sb.append(str[i]);
                // System.out.printf("case 1, %d %d %s\n", prev, next, sb.toString());
                continue;
            }
            // next is not an alphabet
            if (next == 2) {
                sb.append(str[i]);
                // System.out.printf("case 2, %d %d %s\n", prev, next, sb.toString());
                continue;
            }
            // next is an alphabet, prev was not +
            // next is uppercase and prev was lowercase
            if (prev == -1 || prev == 2 || prev == 0) {
                if (sb.length() != 0) {
                    sb.append(' ');
                }
                sb.append(Character.toUpperCase(str[i]));
                // System.out.printf("case 3, %d %d %s\n", prev, next, sb.toString());
                continue;
            }
            // next is lowercase and prev was uppercase
            if (prev == 1) {
                if (capRepeated) {
                    sb.insert(sb.length() - 1, ' ');
                    capRepeated = false;
                }
                sb.append(str[i]);
                // System.out.printf("case 4, %d %d %s\n", prev, next, sb.toString());
            }
        }
        String output = sb.toString().trim();
        output = (output.length() == 0) ? text : output;
        //return output;

        // Capitalize all words (Optional)
        String[] result = output.split(" ");
        for (int i = 0; i < result.length; ++i) {
            result[i] = result[i].charAt(0) + result[i].substring(1).toLowerCase();
        }
        output = String.join(" ", result);
        return output;
    }

    /**
     * Test method for the titleCase() function.
     */
    public static void testTitleCase() {
        System.out.println("--------------- Title Case Tests --------------------");
        String[][] samples = {
            {null, null},
            {"", ""},
            {"a", "A"},
            {"aa", "Aa"},
            {"aaa", "Aaa"},
            {"aC", "A C"},
            {"AC", "Ac"},
            {"aCa", "A Ca"},
            {"ACa", "A Ca"},
            {"aCamel", "A Camel"},
            {"anCamel", "An Camel"},
            {"CamelCase", "Camel Case"},
            {"camelCase", "Camel Case"},
            {"snake_case", "Snake Case"},
            {"toCamelCaseString", "To Camel Case String"},
            {"toCAMELCase", "To Camel Case"},
            {"_under_the_scoreCamelWith_", "Under The Score Camel With"},
            {"ABDTest", "Abd Test"},
            {"title123Case", "Title123 Case"},
            {"expect11", "Expect11"},
            {"all0verMe3", "All0 Ver Me3"},
            {"___", "___"},
            {"__a__", "A"},
            {"_A_b_c____aa", "A B C Aa"},
            {"_get$It132done", "Get It132 Done"},
            {"_122_", "122"},
            {"_no112", "No112"},
            {"Case-13title", "Case13 Title"},
            {"-no-allow-", "No Allow"},
            {"_paren-_-allow--not!", "Paren Allow Not"},
            {"Other.Allow.--False?", "Other Allow False"},
            {"$39$ldl%LK3$lk_389$klnsl-32489  3 42034 ", "39 Ldl Lk3 Lk389 Klnsl32489342034"},
            {"tHis will BE MY EXAMple", "T His Will Be My Exa Mple"},
            {"stripEvery.damn-paren- -_now", "Strip Every Damn Paren Now"},
            {"getMe", "Get Me"},
            {"whatSthePoint", "What Sthe Point"},
            {"n0pe_aLoud", "N0 Pe A Loud"},
            {"canHave SpacesThere", "Can Have Spaces There"},
            {"  why_underScore exists  ", "Why Under Score Exists"},
            {"small-to-be-seen", "Small To Be Seen"},
            {"toCAMELCase", "To Camel Case"},
            {"_under_the_scoreCamelWith_", "Under The Score Camel With"},
            {"last one onTheList", "Last One On The List"},
            {"a string", "A String"},
            {"maRTin o'maLLEY", "Ma R Tin O Ma Lley"},
            {"john wilkes-booth", "John Wilkes Booth"},
            {"YET ANOTHER STRING", "Yet Another String"}
        };
        int pass = 0;
        for (String[] inp : samples) {
            String out = titleCase(inp[0]);
            //String out = WordUtils.capitalizeFully(inp[0]);
            System.out.printf("TEST '%s'\nWANTS '%s'\nFOUND '%s'\n", inp[0], inp[1], out);
            boolean passed = (out == null ? inp[1] == null : out.equals(inp[1]));
            pass += passed ? 1 : 0;
            System.out.println(passed ? "-- PASS --" : "!! FAIL !!");
            System.out.println();
        }
        System.out.printf("\n%d Passed, %d Failed.\n", pass, samples.length - pass);
    }

    public static void main(String[] args) {
        // run tests
        testTitleCase();
    }
}
