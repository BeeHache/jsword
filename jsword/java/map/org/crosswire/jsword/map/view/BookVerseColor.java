
package org.crosswire.jsword.map.view;

import java.awt.Color;

import org.crosswire.jsword.passage.Books;
import org.crosswire.jsword.passage.NoSuchVerseException;

/**
* BookVerseColor gives a color to a selected books, leaving the
* others grey. 
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
* @version D0.I0.T0
*/
public class BookVerseColor implements VerseColor
{
    /**
    * Basic constructor
    */
    public BookVerseColor(int book)
    {
        this.book = book;
    }

    /**
    * Accessor for the currently highlighted book
    * @param book The new highlighted book
    */
    public void setBook(int book)
    {
        this.book = book;
    }

    /**
    * Accessor for the currently highlighted book
    * @return The current highlighted book
    */
    public int getBook()
    {
        return book;
    }

    /**
    * What Color should we use to represent this verse
    * @param book The book number (Gen=1, Rev=66)
    * @param chapter The chapter number
    * @param verse The verse number
    * @return The Color for this verse
    */
    public Color getColor(int book, int chapter, int verse)
    {
        if (book != this.book)
        {
            return Color.gray;
        }
        else
        {
            return Color.red;
        }
    }

    /**
    * What Color would set off the Verses painted on it
    * @return An appropriate background color
    */
    public Color getBackground()
    {
        return Color.black;
    }

    /**
    * What Color should text be painted in
    * @return An appropriate font color
    */
    public Color getForeground()
    {
        return Color.white;
    }

    /**
    * The name for display in a combo box
    */
    public String toString()
    {
        try
        {
            return "Book - "+Books.getLongBookName(book);
        }
        catch (NoSuchVerseException ex)
        {
            return "Book - Error";
        }
    }

    /** The currently highlighted book */
    private int book;
}