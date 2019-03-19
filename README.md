# Abstract

Dieses Multi-Module-Projekt implementiert ggw. [Shamir's Secret Sharing](https://en.wikipedia.org/wiki/Shamir%27s_Secret_Sharing) Algorithmus unter 
Verwendung von Newton's [Polynominterpolation](https://de.wikipedia.org/wiki/Polynominterpolation#Newtonscher_Algorithmus). Darauf aufbauend wird
eine [KeyStore](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/KeyStore.html) Engine im Rahmen der
[Java Cryptographic Architecture](https://docs.oracle.com/en/java/javase/11/security/howtoimplaprovider.html) entwickelt die man außer mit dem Masterpasswort 
auch mit einer Untermenge von Shares des Masterpasswortes freischalten kann. Um das Masterpasswort rekonstruieren zu können, sind eine Anzahl von
Secret Shares zu kombinieren die über einem zuvor festgelegten Schwellenwert liegen. Das Schema ist informationstheoretisch sicher, d.h. mit weniger Secret 
Shares als durch den Schwellenwert definiert, erlangen Angreifer keinerlei zusätzliche Information über das verwendete Masterpasswort. Auf diese Weise kann 
z.B. das [Vier-Augen-Prinzip](https://de.wikipedia.org/wiki/Vier-Augen-Prinzip) durchgesetzt werden.

# Build

Für den Build wird [Maven](https://maven.apache.org/) und ein JDK11+ benötigt:

`mvn clean install`

führt dann die Unit-Tests aus erzeugt die Library mit dem JCA-Provider.