I parametri con i quali è stato effettuato questo test sono i seguenti:

/** Node coverage test parameters */

static final private int NC_INF_SEARCH_NUMBER = 1000;
static final private int NC_SUP_SEARCH_NUMBER = 10000;
static final private int NC_STEP_LENGTH = 500;
static final private double[] NC_PROFILES = {0.125, 0.25, 0.5};
static final private int NC_CACHE_ELEMENT_NUMBER = 4;
static final private int NC_COVER_SEARCH_NUMBER = 4;

Testo ogni profilo effettuando da 1000 a 10000 ricerche aumentando ogni volta di 1000 ricerche ( 1000, 2000, 3000, ..., 10000)

I file prodotti sono 4 per ogni profilo ( uno per ogni livello ( 2 livelli) sia per i VID che per i PID ) quindi ho in totale 12 file visto che i profili testati sono i 3 soprariportati.

I nomi dei file hanno la seguente struttura:

Pid_Coverage_0.125_4_4_3.log

Pid_Coverage	: indica che ho salvato la copertura dei pid altrimenti è uguale a Vid nel caso il file contenga la copertura dei vid

0.5 		: il profile utilizzato
4		: numero di elementi in cache
4		: numero di cover searches
3		: livello a cui fanno riferimento i Pid/Vid salvati

I dati sono memorizzati nel file secondo lo schema seguente:

1a colonna - numero di ricerche
2a colonna - numero nodi coperti / numero nodi totali
3a colonna - numero nodi coperti
4a colonna - numero nodi totali