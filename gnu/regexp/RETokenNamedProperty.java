/* gnu/regexp/RETokenNamedProperty.java
   Copyright (C) 2006 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package gnu.regexp;

final class RETokenNamedProperty extends REToken {
  String name;
  boolean insens;
  boolean negate;
  Handler handler;

  RETokenNamedProperty(int subIndex, String name, boolean insens, boolean negate) throws REException {
    super(subIndex);
    this.name = name;
    this.insens = insens;
    this.negate = negate;
    handler = getHandler(name); 
  }

    int getMinimumLength() {
	return 1;
    }

    boolean match(CharIndexed input, REMatch mymatch) {
    char ch = input.charAt(mymatch.index);
    if (ch == CharIndexed.OUT_OF_BOUNDS)
      return false;
    
    boolean retval = handler.includes(ch);
    if (insens) {
        retval = retval ||
                 handler.includes(Character.toUpperCase(ch)) ||
                 handler.includes(Character.toLowerCase(ch));
    }

    if (negate) retval = !retval;
    if (retval) {
	++mymatch.index;
	return next(input, mymatch);
    }
    else return false;
  }

  void dump(StringBuffer os) {
    os.append("\\")
      .append(negate ? "P" : "p")
      .append("{" + name + "}");
  }

  private abstract static class Handler {
      public abstract boolean includes(char c);
  }

  private Handler getHandler(String name) throws REException {
      if (name.equals("Lower") ||
          name.equals("Upper") ||
          // name.equals("ASCII") ||
          name.equals("Alpha") ||
          name.equals("Digit") ||
          name.equals("Alnum") ||
          name.equals("Punct") ||
          name.equals("Graph") ||
          name.equals("Print") ||
          name.equals("Blank") ||
          name.equals("Cntrl") ||
          name.equals("XDigit") ||
          name.equals("Space") ) {
         return new POSIXHandler(name);
      }
      if (name.startsWith("In")) {
          throw new REException("Unicode block is not supported yet", REException.REG_ESCAPE, 0); 
      }
      if (name.startsWith("Is")) {
          name = name.substring(2);
      }
      if (name.equals("Mc"))
          return new UnicodeCategoryHandler(Character.COMBINING_SPACING_MARK);
      if (name.equals("Pc"))
          return new UnicodeCategoryHandler(Character.CONNECTOR_PUNCTUATION);
      if (name.equals("Cc"))
          return new UnicodeCategoryHandler(Character.CONTROL);
      if (name.equals("Sc"))
          return new UnicodeCategoryHandler(Character.CURRENCY_SYMBOL);
      if (name.equals("Pd"))
          return new UnicodeCategoryHandler(Character.DASH_PUNCTUATION);
      if (name.equals("Nd"))
          return new UnicodeCategoryHandler(Character.DECIMAL_DIGIT_NUMBER);
      if (name.equals("Me"))
          return new UnicodeCategoryHandler(Character.ENCLOSING_MARK);
      if (name.equals("Pe"))
          return new UnicodeCategoryHandler(Character.END_PUNCTUATION);
      if (name.equals("Pf"))
          return new UnicodeCategoryHandler(Character.FINAL_QUOTE_PUNCTUATION);
      if (name.equals("Cf"))
          return new UnicodeCategoryHandler(Character.FORMAT);
      if (name.equals("Pi"))
          return new UnicodeCategoryHandler(Character.INITIAL_QUOTE_PUNCTUATION);
      if (name.equals("Nl"))
          return new UnicodeCategoryHandler(Character.LETTER_NUMBER);
      if (name.equals("Zl"))
          return new UnicodeCategoryHandler(Character.LINE_SEPARATOR);
      if (name.equals("Ll"))
          return new UnicodeCategoryHandler(Character.LOWERCASE_LETTER);
      if (name.equals("Sm"))
          return new UnicodeCategoryHandler(Character.MATH_SYMBOL);
      if (name.equals("Lm"))
          return new UnicodeCategoryHandler(Character.MODIFIER_LETTER);
      if (name.equals("Sk"))
          return new UnicodeCategoryHandler(Character.MODIFIER_SYMBOL);
      if (name.equals("Mn"))
          return new UnicodeCategoryHandler(Character.NON_SPACING_MARK);
      if (name.equals("Lo"))
          return new UnicodeCategoryHandler(Character.OTHER_LETTER);
      if (name.equals("No"))
          return new UnicodeCategoryHandler(Character.OTHER_NUMBER);
      if (name.equals("Po"))
          return new UnicodeCategoryHandler(Character.OTHER_PUNCTUATION);
      if (name.equals("So"))
          return new UnicodeCategoryHandler(Character.OTHER_SYMBOL);
      if (name.equals("Zp"))
          return new UnicodeCategoryHandler(Character.PARAGRAPH_SEPARATOR);
      if (name.equals("Co"))
          return new UnicodeCategoryHandler(Character.PRIVATE_USE);
      if (name.equals("Zs"))
          return new UnicodeCategoryHandler(Character.SPACE_SEPARATOR);
      if (name.equals("Ps"))
          return new UnicodeCategoryHandler(Character.START_PUNCTUATION);
      if (name.equals("Cs"))
          return new UnicodeCategoryHandler(Character.SURROGATE);
      if (name.equals("Lt"))
          return new UnicodeCategoryHandler(Character.TITLECASE_LETTER);
      if (name.equals("Cn"))
          return new UnicodeCategoryHandler(Character.UNASSIGNED);
      if (name.equals("Lu"))
          return new UnicodeCategoryHandler(Character.UPPERCASE_LETTER);
      throw new REException("unsupported name " + name, REException.REG_ESCAPE, 0);
  }

  private static class POSIXHandler extends Handler {
      private RETokenPOSIX retoken;
      private REMatch mymatch = new REMatch(0,0,0);
      private char[] chars = new char[1];
      private CharIndexedCharArray ca = new CharIndexedCharArray(chars, 0);
      public POSIXHandler(String name) {
            int posixId = RETokenPOSIX.intValue(name.toLowerCase());
            if (posixId != -1)
              retoken = new RETokenPOSIX(0,posixId,false,false);
	    else
              throw new RuntimeException("Unknown posix ID: " + name);
      }
      public boolean includes(char c) {
          chars[0] = c;
          mymatch.index = 0;
          return retoken.match(ca, mymatch);
      }
  }

  private static class UnicodeCategoryHandler extends Handler {
      public UnicodeCategoryHandler(byte category) {
          this.category = (int)category;
      }
      private int category;
      public boolean includes(char c) {
          return Character.getType(c) == category;
      }
  }
 
}