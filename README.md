Nutikas restorani reserveerimissüsteem.
Testülesanne CGI suvepraktikale.

Projekti üldkirjeldus:
Tegemist on veebirakendusega, mis võimaldab restoranis laudu broneerida.

Kasutaja saab:
1)Valida kuupäeva ja kellaaja
2)Määrata inimeste arvu
3)Valida eelistused (privaatsus, aknakoht, tsoon jne)

Süsteem analüüsib saadaolevaid laudu ja soovitab kõige sobivama variandi, kuvades selle saaliplaanil. Hõivatud lauad genereeritakse automaatselt. Soovitatud laud on visuaalselt esile tõstetud.

Täiendavalt on realiseeritud:
1)Laudade dünaamiline ühendamine suuremate seltskondade jaoks
2)Külastuse kestuse arvestamine
3)Admin-režiim laudade paigutuse muutmiseks
4)Dockeri konteiner rakenduse käivitamiseks

Kasutatud tehnoloogiad:
Backend:
Java (LTS versioon)
Spring Boot
Maven
H2 andmebaas

Frontend:
HTML
CSS
JavaScript

Täiendavalt:
Docker

Minu kogemus:
Projekt on realiseeritud Java (Spring Boot) abil. See on minu esimene suurem backend-projekt Java keeles. Minu põhiline programmeerimiskeel, mida õpin ülikoolis, on C#.Seetõttu pidin projekti käigus täiendavalt süvenema:Java süntaksisse,Spring Booti,JPA-sse,annotatsioonidesse ja backend-rakenduse struktuuri.
JavaScripti olen varem kasutanud piiratud mahus (peamiselt frontend’i jaoks).

AI-tööriistade kasutamine:
Arenduse käigus kasutasin GitHub Copilotit VS Code’is abivahendina töö kiirendamiseks.

Copilot aitas:
Genereerida mallikoodi (getterid, setterid, konstruktorid),pakkuda Java Stream API süntaksit,täiendada frontend’i fetch-päringuid,genereerida baasilisi CSS-stiile

Samas:
Projekti arhitektuuri kavandasin iseseisvalt,laudade soovitusalgoritmi äriloogika on minu poolt realiseeritud,kogu genereeritud kood vaadati läbi, toimetasin oma parimate võimete ja teadmiste kohaselt ning analüüsisin enne kasutamist.

Laudade soovitamise algoritm:
Parima laua valimiseks kasutatakse hindamissüsteemi (score).
Arvesse võetakse kahte peamist tegurit:

1) Efektiivne kasutus:
Mida lähemal on laua mahutavus külastajate arvule, seda kõrgem on hinne.
Väikesele seltskonnale ei pakuta suuri laudu, kui sobivamaid variante on saadaval

2) Kasutaja eelistused:
Privaatsus,tsoon,muud parameetrid,iga laud saab lõpphinde, mille alusel valitakse kõige sobivam variant.

Mis oli keeruline
1. Laudade hindamisalgoritm:
Keeruline oli tasakaalustada efektiivset ruumikasutust ja kliendi eelistusi.
Lahenduseks kasutasin kaalutud punktisüsteemi.

2. Laudade dünaamiline ühendamine:
Tuli määratleda, milliseid laudu saab pidada “naabriteks” (koordinaatide alusel).Selleks tuli katsetada erinevaid kauguse väärtusi.

3. Töö Java ja Spring Bootiga
Kuna Java on minu jaoks uus keel, kulus osa ajast kohanemisele ja ökosüsteemi õppimisele.Osaliselt realiseeritud või edasiarendamist vajavad funktsioonidBroneeringute ajavahemike kattuvuse kontrolli saab täiustada.
Võimalik on lisada automaatne laudade vabastamine pärast kindlat ajaperioodi ja
admin-liidest saab laiendada (drag-and-drop koos reaalajas salvestamisega)

Projekti struktuur:
backend/
 ├── controller   # REST API
 ├── service      # Äriloogika
 ├── model        # Andmemudelid
 ├── repository   # JPA repositooriumid
 └── static       # Frontend

Git-ajalugu:
Arendus toimus etapiviisiliselt regulaarsete commit’idega:
Restaurant reservation system — algversioon
Add dynamic table pairing for large party reservations
Fix tables search and layout
Add admin layout
Add visit-duration & improve recommendation filters
Add docker container
small refactoring

Ajakulu:
Ligikaudu 30–35 tundi.Osa ajast kulus Java ja Spring Booti õppimisele, kuna tegemist on minu esimese suurema projektiga selles tehnoloogias.