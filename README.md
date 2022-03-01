# Hidden Image Data Encoding

Code des Prototypen zum **Blogbeitrag**. https://blog.doubleslash.de/steganographie-als-sichere-alternative-zu-metadaten/

Umfasst die Encoding - API und eine rudimentäre Benutzeroberfläche. Mit diesem Programm lassen sich PNG - Grafiken, wie im Blogbeitrag beschieben, mit benutzerdefinierten Metadaten und einer Signatur versehen, welche mithilfe des TBS - Verfahrens in die Farbinformation des Bildes kodiert werden. 

Die Signatur bricht bereits bei der Änderung eines einzelnen beliebigen Pixels um minimale Werte. Durch die Signaturprüfung lassen sich Bildmodifikationen nachweisen.

## API und UI

Die Bibliothek ist unter hide-api zu finden, die Testanwendung unter hide-ui. 

Schnittstelle der API ist die ImageHandler - Klasse (hide-api/src/main/java/de/doubleslash/hideAPI/ImageHandler)

Anwender der API ist die UserLogic - Klasse (hide-ui/src/main/java/de/doubleslash/hide/UserLogic)

## Build

Das Repository hat eine Maven - Projektstruktur und lässt sich direkt importieren.

**Voraussetzungen:**
* JDK 1.8
* Maven 3
* [GPG - RSA - Key](https://gnupg.org/)

Für das Signaturverfahren wird der erste, gültige asymmetrische GPG - Key verwendet, der gefunden werden kann. Ohne lokalen GPG - Key lässt sich das Signaturverfahren nicht anwenden. Empfohlen wird GPG-RSA-2096.
Die [Windows - Implementierung von GPG](https://www.gpg4win.org/) wird momentan nicht unterstützt.



### Disclaimer

Dieses Projekt ist ein proof of concept. Die Funktionen und Implementierungen sind experimentell und nicht frei von Bugs.

Die automatische Bildbeschreibung anhand des [ClarifAI](https://www.clarifai.com/) Service benötigt einen eigenen Key, der in der MessageHandler - Klasse eingetragen werden muss. Dieser kann auf der Homepage erstellt und kostenfrei genutzt werden.

Die Funktionen "benutzerdefinierte Metadaten" und "Signatur" zu kombinieren kann u.U. zu fehlerhaften Ergebnissen führen.

### Lizenz

[GNU GPL v3](https://www.gnu.org/licenses/gpl.html)
