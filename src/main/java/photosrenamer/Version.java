/*
 * Copyright (c) 2004-2021, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package photosrenamer;

/**
 * Version information for this product. Has methods to obtain information about the product, as
 * well as a main method, so it can be called from the command line.
 *
 * @author Sualeh Fatehi
 */
public final class Version {

  private static final String PRODUCTNAME = "Photos Renamer";
  private static final String VERSION = "1.4.1";

  /**
   * Information about this product.
   *
   * @return Information about this product.
   */
  public static String about() {
    final StringBuffer about = new StringBuffer();

    about
        .append(getProductName())
        .append(" ")
        .append(getVersion())
        .append("\n")
        .append("\u00A9 2004-2021 Sualeh Fatehi");

    return new String(about);
  }

  /**
   * Product name.
   *
   * @return Product name.
   */
  public static String getProductName() {
    return PRODUCTNAME;
  }

  /**
   * Product version number.
   *
   * @return Product version number.
   */
  public static String getVersion() {
    return VERSION;
  }

  /**
   * Main routine. Prints information about this product.
   *
   * @param args Arguments to the main routine - they are ignored.
   */
  public static void main(final String[] args) {
    System.out.println(about());
  }

  private Version() {}

  /**
   * String representation. Information about this product.
   *
   * @return String representation.
   */
  @Override
  public String toString() {
    return about();
  }
}
