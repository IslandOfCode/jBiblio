# jBiblio

Un semplice gestore per piccole librerie scolastiche

## Codice sorgente e API

Dopo aver importato il progetto, è necessaria una veloce modifica prima di testare il codice.

In `src/main/resources/` è presente un file chiamato `_api.properties`.

Questo file contiene tutte le key per l'uso delle API. Attualmente è presente la sola API per Google Books.

Per poter usare il programma è necessario avere a disposizione una chiave per Google Books, procedere quindi nel seguente modo:

1. Accedere alla propria [Google Developer Console](https://console.developers.google.com/?hl=IT) (*è necessario un account google*).
2. Generare una key per le api Google Books (*potrebbe essere necessario creare prima delle credenziali*).
3. Rinominare il file `_api.properties` in `api.properties`, rimuovendo così l'underscore iniziale.
4. Aprire il file ed incollare la propria chiave alla voce **GBOOKS_API**

Ora si può avviare jBiblio e testare la funzione di ricerca automatica delle informazioni del libro tramite ISBN.

## Primo avvio

Il programma è _standalone_, cioè non ha bisogno di essere installato. Essendo scritto in Java, è necessario che
una JRE (o anche JDK) sia presente sulla macchina.

### Prerequisiti

Se non è già presente, è necessario scaricare l'ambiente java.

Sul sito:
[java.com/download](https://www.java.com/it/download/)
è possibile scaricare la versione più aggiornata.

**Attenzione!** jBiblio necessita di una versione di java pari o superiore alla 1.8 per poter essere eseguito.
Anche se avete una versione compatibile, è sempre consigliabile scaricare l'ultimo _update_ disponibile.


### Download e installazione

Nessuna installazione necessaria, seguire le indicazioni della sezione **Aggiornamento**, presente più in basso, per le istruzioni al download.

### Avvio

Per avviare jEBill, è sufficente fare doppio click sul file jar o sul file eseguibile (*questo dipende da quale versione abbiate scaricato*).
Dopo qualche attimo di attesa, la finestra principale del programma dovrebbe apparire.

In caso di attese più lunghe di qualche secondo, verificare se la cartella **logs** è stata creata. Se si, allora aprire il file con il numero sequenza più elevato.
Ad esempio, log_0.txt è il primo file che viene creato al primo avvio.

In questo file potrebbe essere possibile individuare il problema.

## Manutenzione

Il programma crea due cartelle supplementari all'avvio (**se queste non sono presenti**).

- **db** contiene il file *jbiblio.db*, database **SQLite** che contiene tutte le informazioni su libri, clienti e prestiti.
- **logs** contiene i file di log del programma.

Cancellando la cartella "db" o il file "jbiblio.db" in esso contenuto, perderete **TUTTE** le informazioni inserite nel programma.
Risulta fondamentale effettuare delle copie di sicurezza ad intervalli regolari per evitare perdite totali dei dati.

La cartella "logs" e i file in esso contenuti possono essere rimossi senza problemi di sorta (**ovviamente a programma NON avviato**).

I file di log risultano però fondamentali per rilevare errori del programma, quindi è consigliabile mantenere una copia di questa cartella in caso di problemi, da fornire allo sviluppatore per ulteriori analisi.

## Aggiornamento

Dalla pagina Github, recarsi nella sezione Release. Qui dovrebbe essere presente l'ultima versione stabile possibile.

Se si vuole utilizzare una versione in via di sviluppo, ma certamente più aggiornata, allora è necessario clonare il repository.

## Uso

Questa sezione non è ancora pronta.

## Companion App jBiblio Scan
jBiblio è dotato (a partire dalla versione 0.9.5-beta) di meccanismo di scansione di codici ISBN tramite applicazione Android.

Questa applicazione può essere reperita dalla sezione [jBiblio-Scan Release](https://github.com/IslandOfCode/jBiblioScan/releases) del repository github relativo.

Il codice sorgente, invece, è possibile visionarlo [qui](https://github.com/IslandOfCode/jBiblioScan).

Si rimanda al suo file README per informazioni più approfondite sull'app.

### Uso della companion App.
Queste le istruzioni temporanee per l'uso dell'app.

- Avviare jBiblio e usare la voce di menù `jBiblio/Connetti app`
- Si dovrebbe aprire una piccola finestra con un codice QR. Questo codice codifica ip e porta su cui il programma è raggiungibile

**Nota bene:** Attualmente il programma utilizza la porta **6339** per la connessione e trasmissione di rete e non è possibile cambiarla.

- Sul vostro smartphone, **assicuratevi di essere connessi alla stessa rete WIFI del computer su cui è aperto jBiblio.**
- Avviare la companion app sul vostro smartphone, attendere la scomparsa dello splashscreen e cliccare sul tasto Connetti. Se all'apertura vi viene chiesto di concedere i permessi di rete, fatelo.
- Si aprirà una seconda schermata (la prima volta vi verrà chiesto di concedere i permessi per l'uso della fotocamera, fatelo) con l'inquadratura della fotocamera in tempo reale.
- Inquadrate il codice QR a schermo e attendente un suono di notifica. Se tutto va bene, la finestra con il codice QR dovrebbe chiudersi e l'app dovrebbe ritornare alla prima schermata.
- Noterete che su jBiblio, in alto a destra della finestra principale, c'è una scritta verde _App connessa_ e che sull'app adesso potete disconnettervi invece che connettervi.

**Per scansionare un codice a barre ISBN:**

- Cliccate sul tasto dell'app **Scansiona codice**
- Si aprirà una schermata identica a quella della connessione, con la differenza che questa può solo scansionare codici a barre EAN-13 (tipici degli ISBN).
- Inquadrate il codice a barre, attendete che l'autofocus metta a fuoco l'immagine e che venga eseguito un suono di notifica.

**Attenzione**: se l'autofocus ha difficoltà a mettere a fuoco il codice a barre, provate ad allontare lo smartphone di qualche centimetro, oppure ad inquadrate un'altra parte del libro prima di ritornare al codice a barre.
- Un messaggio vi chiederà se scansionare un secondo codice a barre. Se cliccate di no, l'app tornerà alla finestra principale.

## Autore

**Pier Riccardo Monzo** - *One man team* - [IslandOfCode.it](https://www.islandofcode.it/)

## Licenza

Questo programma è sviluppato sotto licenza **GPLv3**.

Fate riferimento al file [LICENSE.md](LICENSE.md), per avere sempre la versione più aggiornata della licensa.

Si ricorda che, se avete accettato la licenza, è obbligatorio averne SEMPRE una copia.