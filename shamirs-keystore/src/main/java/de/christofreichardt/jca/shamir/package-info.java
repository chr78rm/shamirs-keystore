/**
 * Comprises a JCA KeyStore engine and related classes. In order to use the provider within your application
 * <ol>
 *     <li>
 *         Put the provider jar and its transitive dependencies on the classpath of your application. Typically
 *         this is done by Maven for you.
 *     </li>
 *     <li style="margin-top: 0.5em">
 *         Dynamically register the provider with
 *         <div style="margin-top: 0.5em">
 *              <code>
 *                  ShamirsProvider myProvider = new ShamirsProvider();
 *              </code>
 *              <br>
 *              <code>
 *                  Security.addProvider(myProvider);
 *              </code>
 *         </div>
 *     </li>
 *     <li style="margin-top: 0.5em">
 *         Now you can request a KeyStore instance of type "ShamirsKeystore" with
 *         <div style="margin-top: 0.5em">
 *             <code>
 *                 KeyStore keyStore = KeyStore.getInstance("ShamirsKeystore", myProvider);
 *             </code>
 *         </div>
 *     </li>
 * </ol>
 *
 * @see
 * <a href="https://docs.oracle.com/en/java/javase/21/security/java-cryptography-architecture-jca-reference-guide.html#GUID-2BCFDD85-D533-4E6C-8CE9-29990DEB0190">
 *      Java Cryptography Architecture (JCA) Reference Guide
 * </a>
 *
 * @see
 * <a href="https://chr78rm.github.io/shamirs-keystore/">
 *      Shamir's Keystore
 * </a>
 */
package de.christofreichardt.jca.shamir;