
package org.crosswire.jsword.control.search;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
* Our command line parsing is a little specialized, so StringTokenizer is not
* up to the job. The specific problem is that there is sometimes no separator
* between parts of the command, and since this is specialized we also leave the
* results in a Vector of SearchWords.
*
* <table border='1' cellPadding='3' cellSpacing='0' width="100%">
* <tr><td bgColor='white'class='TableRowColor'><font size='-7'>
* Distribution Licence:<br />
* Project B is free software; you can redistribute it
* and/or modify it under the terms of the GNU General Public License,
* version 2 as published by the Free Software Foundation.<br />
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* General Public License for more details.<br />
* The License is available on the internet
* <a href='http://www.gnu.org/copyleft/gpl.html'>here</a>, by writing to
* <i>Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
* MA 02111-1307, USA</i>, Or locally at the Licence link below.<br />
* The copyright to this program is held by it's authors.
* </font></td></tr></table>
* @see <a href='http://www.eireneh.com/servlets/Web'>Project B Home</a>
* @see <{docs.Licence}>
* @author Joe Walker
*/
public class CustomTokenizer
{
    /**
    * Convenience method to generate a Vector of SearchWords
    * @param sought The text to parse
    * @param commands The Hashtable of SearchWords to select from
    * @return An Vector of selected SearchWords
    */
    public static Vector tokenize(String sought, Hashtable commands) throws SearchException
    {
        Vector output = new Vector();
        String command_chars = getSingleCharWords(commands);
        int current_type = charType(sought.charAt(0), command_chars);
        int start_index = 0;

        // If the first character is a [ then we have a problem because
        // the loop starts with the second character because it needs
        // something to compare with - so if we do start with a [ then
        // we make sure that we perpend with a " "
        if (sought.length() > 0 && sought.charAt(0) == '[')
        {
            sought = " " + sought;
        }

        // Loop, comparing each character with the previous one
        for (int i=1; i<=sought.length(); i++)
        {
            // An escaped section
            if (i != sought.length() && sought.charAt(i) == '[')
            {
                int end = sought.indexOf("]", i);
                if (end == -1)
                    throw new SearchException("search_unmatched_escape");

                addWord(output, commands, "[");
                addWord(output, commands, sought.substring(i+1, end));
                addWord(output, commands, "]");

                current_type = CHAR_SPACE;
                i = end + 1;
            }

            // If this is the last word then so long as this letter is
            // not space (in which case it has been added already) then
            // all the word in
            if (i == sought.length())
            {
                if (current_type != CHAR_SPACE)
                    addWord(output, commands, sought.substring(start_index));
            }
            else
            {
                // If this is the start of a new section of the command
                // then add the word in
                int new_type = charType(sought.charAt(i), command_chars);
                if (current_type != new_type || new_type == CHAR_COMMAND)
                {
                    if (current_type != CHAR_SPACE)
                        addWord(output, commands, sought.substring(start_index, i));

                    start_index = i;
                    current_type = charType(sought.charAt(i), command_chars);
                }
            }
        }

        return output;
    }

    /**
    * What class of character is this?
    * @param sought The string to be searched for
    * @return The chatacter class
    */
    private final static int charType(char sought, String commands)
    {
        if (Character.isWhitespace(sought))     return CHAR_SPACE;
        if (commands.indexOf(""+sought) != -1)  return CHAR_COMMAND;
        else                                    return CHAR_PARAM;
    }

    /**
    * Convenience function to add a SearchWord to the Vector being created.
    * @param output The Vector to alter
    * @param commands The SearchWord source
    * @param word The trigger to look for
    */
    private static void addWord(Vector output, Hashtable commands, String word)
    {
        Object word_obj = commands.get(word);
        if (word_obj == null) word_obj = new DefaultParamWord(word);
        output.addElement(word_obj);
    }

    /**
    * Convenience function to add a SearchWord to the Vector being created.
    * @param output The Vector to alter
    * @param commands The SearchWord source
    * @param word The trigger to look for
    */
    private static String getSingleCharWords(Hashtable commands)
    {
        Enumeration en = commands.keys();
        StringBuffer buf = new StringBuffer();

        while (en.hasMoreElements())
        {
            String cmd = (String) en.nextElement();
            if (cmd.length() == 1) buf.append(cmd);
        }

        return buf.toString();
    }

    /** The type of character (see charType) */
    private static final int CHAR_PARAM = 0;

    /** The type of character (see charType) */
    private static final int CHAR_COMMAND = 1;

    /** The type of character (see charType) */
    private static final int CHAR_SPACE = 2;
}