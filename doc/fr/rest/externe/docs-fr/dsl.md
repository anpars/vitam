Le DSL (Domain Specific Language) de la solution logicielle Vitam est le langage dédié à l'interrogation de la base de données et du moteur d'indexation et offrant un niveau d'abstraction afin de pouvoir exprimer un grand nombre de possibilités de requêtes. Sa structure est composée de deux parties :

- Request : contient la structure du Body contenant la requête au format JSON. Le DSL permet d'exprimer un grand nombre de possibilités de requêtes. Dans le cadre des collections Metadata (Units et Objects), cette requête peut être organisée comme un ensemble de sous requêtes.
- Response : contient la structure du Body contenant le résultat au format JSON. Il contient les différentes informations demandées.

# Request

La requête DSL est transmise dans le Body en tant que JSON (application/json).

## Format des requêtes

### Requêtes de Rercherche
Une requête DSL de **recherche** se décompose en plusieurs sections selon l'opération souhaitée :

- **$query** : la requête, composée de critères de sélection
- **$roots** : les racines à partir desquels la recherche est lancée, uniquement pour les collections units et objects, dont les données sont organisées en mode arborescent
- **$filter** : le tri / la limite en nombre de résultats retournés
- **$projections** : un sous ensemble de champs devant être retournés

Pour comparaison avec le langage SQL :

```sql
SELECT field1, field2         /* la Projection */
FROM table1                   /* la Collection */
WHERE field3 < value1         /* la partie Query */
LIMIT n ORDER BY field1 ASC   /* les Filtres */
```

#### Requêtes de recherche pour un élément spécifique (GET BY ID)
La requête DSL **GET BY ID** est la plus simple. Elle permet de renvoyer le contenu (ou un partie) d'un élément spécifique pour lequel l'identifiant est spécifié dans l'URL. Seule la section **$projection** peut être renseignée.

```JSON
{
  "$projection": { }
}
```

**Exemple :** Récupération d'une unité archivistique donnée (GET access-external/v1/units/GUID1) :
- en revoyant uniquement les champs Title et Description

```JSON
{
  "$projection": {
    "$fields": { "Title": 1, "Description": 1 }
  }
}
```

#### Requêtes de recherche sur les collections Units et Objects (SELECT MULTIPLE)

Les collections de Metadata, que sont les collections units et objects, se requêtent en utilisant un tableau de **$query**.
Une Query correspond à la formulation `WHERE xxx` dans le langage SQL, c'est à dire les critères de sélection.

Le DSL permet d'effectuer des requêtes arborescentes via la spécification de racines et de profondeur de recherche :
- **$roots** : il s'agit de la ou des racines (Units) à partir desquelles la requête est exprimée (toutes les recherches sur les Units et Objects sont en mode arborescente). Il correspond d'une certaine façon à "*FROM x*" dans le langage SQL étendu au cas des arborescences. En l'absence du paramètre, la recherche s'effectuera sur toutes les unités archivistiques.
- **$depth** : spécifie la profondeur maximale dans l'arborescence et à partir des $roots dans laquelle la recherche doit s'exécuter. La valeur de $depth doit être un entier positif ou nul.
  - *$depth = 0* : cherche uniquement sur les unités précisées dans le $roots
  - *$depth =n avec n >0* : cherche sur les unités enfants jusqu'à *n* niveau(x) de profondeur et ne cherche pas dans les unités de $roots elles-mêmes.
  **IMPORTANT :** Le paramètre **$depth** doit être spécifié lorsque des racines sont définies dans **$roots** (recherche arborescente). Le paramètre **$depth** ne doit pas être spécifié lorsque les racines ne sont pas spécifiées (recherche sur toutes les unités).

Il est également possible spécifier des filtres de recherche via **$filter** et des projections **$projection**.

```JSON
{
  "$roots": [ ],
  "$query": [ ],
  "$filter": { },
  "$projection": { }
}
```

##### **Recherche mono-requête**
Il s'agit du mode de recherche nominal. Dans ce mode, **$query** ne contient qu'une seule requête de recherche.
Le résultat de la recherche est directement renvoyé au client.

**Exemples :**

1/ Rechercher les unités ayant "Alpha" dans leurs titres parmi les unités racines ayant pour #id GUID1 ou GUID2

```json
{
  "$roots": [
    "GUID1",
    "GUID2"
  ],
  "$query": [
    {
      "$match": { "Title": "Alpha" },
      "$depth": 0
    }
  ],
  "$filter": {},
  "$projection": {}
}
```

2/ Rechercher les unités ayant "Alpha" dans leurs titres, et étant un enfant direct d'une des unités ayant pour #id GUID1 ou GUID2

```json
{
  "$roots": [
    "GUID1",
    "GUID2"
  ],
  "$query": [
    {
      "$match": { "Title": "Alpha" },
      "$depth": 1
    }
  ],
  "$filter": {},
  "$projection": {}
}
```

3/ Pour effectuer cette recherche uniquement sur les enfants des unités de $roots ayant une profondeur relative de 1 à 5

```json
{
  "$roots": [
    "GUID1",
    "GUID2"
  ],
  "$query": [
    {
      "$match": { "Title": "Alpha" },
      "$depth": 5
    }
  ],
  "$filter": {},
  "$projection": {}
}
```

4/ Rechercher dans toutes les unités archivistiques du système

```json
{
  "$query": [
      { "$match": { "Title": "Alpha" } }
  ],
  "$filter": {},
  "$projection": {}
}
```

##### **Recherche multi-requêtes**

Dans ce mode, **$query** contient plusieurs requêtes de recherche qui seront exécutées successivement (tableau de Query).
La succession est exécutée avec la signification suivante :
- Depuis $roots, chercher les Units/Objects tels que Query[1], conduisant à obtenir une liste d'identifiants[1]
- Cette liste d'identifiants[1] devient le nouveau $roots, chercher les Units/Objects tel que Query[2], conduisant à obtenir une liste d'identifiants[2]
- Et ainsi de suite, la liste d'identifiants[n] de la dernière Query[n] est la liste de résultat définitive renvoyée au client.

**Exemple :** Rechercher les unités ayant "Discours du président" dans leur description, et qui sont des descendants directs des unités racines ayant pour #id GUID1 ou GUID2 et contenant "Alpha" dans leur titre.

```json
{
  "$roots": [
    "GUID1",
    "GUID2"
  ],
  "$query": [
    {
      "$match": { "Title": "Alpha" },
      "$depth": 0
    },
    {
      "$match": { "Description": "Discours du président" },
      "$depth": 1
    }
  ],
  "$filter": {},
  "$projection": {}
}
```

**Notes :**
- La recherche multi-requêtes est actuellement supportée à titre **expérimental**.
- Selon la complexité des sous-requêtes, les recherches multi-requêtes peuvent être très couteuses.
- Les filtres de recherche, et en particulier **$limit** et **$offset**, s'appliquent uniquement sur la dernière requête. Les autres (premières) sous-requêtes ne doivent pas renvoyer un nombre de résultats trop important (10000+) au risque d'un comportement de recherche non déterminé.

#### Requêtes de recherche sur les autres collections (SELECT SINGLE)

Les autres collections (hormis Units et Objects) s'utilisent avec une seule **$query**, et ne contiennent pas de **$roots**, ni de **$depth**.
Il est possible spécifier des filtres de recherche via **$filter** et des projections **$projection**.

```JSON
{
  "$query": { },
  "$filter": { },
  "$projection": { }
}
```
**Exemple :** Rechercher un Contrat d'Entrée avec une description donnée :

```json
{
  "$query": { "$match_phrase": { "Description": "Contrat d'entrée Alpha" } },
  "$filter": {},
  "$projection": {}
}
```
### Requêtes de modification

La requête DSL de mise à jour (**UPDATE BY ID**) permet de modifier le contenu du document pour lequel l'identifiant est spécifié dans l'URL.
Les requêtes DSL de modification contiennent une section **$action**.

```JSON
{
  "$action": [ ]
}
```
**Exemple :** Modifier la description d'un Contrat d'Entrée  :

```json
{
  "$action": [
       { "$set" : { "Description" : "Description" } }
    ]
}
```

## Query

Les commandes de la Query peuvent être :

| Catégorie          | Opérateurs                                              | Arguments                                  | Utilisable sur champs analysés ? | Utilisable sur champs non analysés ? | Commentaire                                                                   |
|--------------------|---------------------------------------------------------|--------------------------------------------|----------------------------------|--------------------------------------|-------------------------------------------------------------------------------|
| Booléens           | $and, $or, $not                                         | Opérateurs                                 | NA                               | NA                                   | Combinaison logique d'opérateurs                                              |
| Comparaison        | $eq, $ne, $lt, $lte, $gt, $gte                          | Champ et valeur                            | Non                              | **Oui**                              | Comparaison de la valeur d'un champ et la valeur passée en argument           |
|                    | $range                                                  | Champ, $lt, $lte, $gt, $gte et valeurs     | Non                              | **Oui**                              | Comparaison de la valeur d'un champ avec l'intervalle passé en argument       |
| Existence          | $exists                                                 | Champ                                      | **Oui**                          | **Oui**                              |                                                                               |
| Tableau            | $in, $nin                                               | Champ, valeurs                             | Non                              | **Oui**                              | Présence de valeurs dans un tableau                                           |
| Textuel            | $wildcard                                               | Champ, expression                          | Non                              | **Oui**                              | Comparaison de champs mots-clefs à valeur exacte                              |
|                    | $regex                                                  | Champ, expression régulière                | Non                              | **Oui**                              | Recherche via une expression régulière                                        |
|                    | $match, $match_all, $match_phrase, $match_phrase_prefix | Champ, phrase                              | **Oui**                          | Non                                  | Recherche plein texte soit sur des mots, expressions, ou débuts d'expressions |
|                    | $search                                                 | Champ, string avec opérateur du $search    | **Oui**                          | Non                                  | Recherche du type moteur de recherche                                         |
| Parcours de graphe | $depth                                                  | entier positif ou nul                      | NA                               | NA                                   | Recherche jusqu'à un niveau de profondeur                                     |

### Opérateurs $and, $or, $not : combinaison logique d'opérateurs

**Format :**
- `{ $and : [ expression1, expression2, ... ] }` où chaque expression est une commande et *toutes* les commandes doivent être vérifiées
- `{ $or  : [ expression1, expression2, ... ] }` où chaque expression est une commande et *au moins une* commande doit être vérifiée
- `{ $not : [ expression1, expression2, ... ] }` où chaque expression est une commande et *aucune* ne doit être vérifiée ($not[condition A, condition B] peut donc s'écrire sous une forme plus explicite de : $and[$not(condition A), $not(condition B)]).

**Exemple :**

```json
{
  "$query": [
    {
      "$or": [
        { "$match": { "Title": "Porte de Bagnolet" } },
        {
          "$and": [
            { "$match": { "Title": "porte" } },
            {
              "$not": [
                { "$match": { "Title": "Chapelle" } }
              ]
            }
          ]
        }
      ]
    }
  ],
  "$filter": {},
  "$projection": { "$fields": { "Title": 1 } }
}
```
Sur la collection units, cette requête demande toutes les unités archivistiques dont le titre est "Porte de bagnolet" ou dont le titre contient "porte" mais pas "chapelle"

### Opérateurs $eq, $ne, $lt, $lte, $gt, $gte : recherche par comparateurs d'égalité

Comparaison de la valeur d'un champ et la valeur passée en argument

**Format :**
- `{ "$eq"  : { name : value } }` : où name est le nom du champ, et value est la valeur recherchée (Equals)
- `{ "$ne"  : { name : value } }` : où name est le nom du champ, et value est la valeur recherchée (Not Equals)
- `{ "$lt"  : { name : value } }` : où name est le nom du champ, et value est la valeur à comparer (Less Than)
- `{ "$lte" : { name : value } }` : où name est le nom du champ, et value est la valeur à comparer (Less Than or Equal)
- `{ "$gt"  : { name : value } }` : où name est le nom du champ, et value est la valeur à comparer (Greater Than)
- `{ "$gte" : { name : value } }` : où name est le nom du champ, et value est la valeur à comparer (Greater Than or Equal)

**Exemples :**

```json
{ "$eq"  : { "Identifier" : "CT-000001" } }
{ "$eq"  : { "StartDate" : "2014-03-25" } }
{ "$ne"  : { "PI" : 3.14 } }
{ "$ne"  : { "Status" : true } }
{ "$lt"  : { "Identifier" : "CT-000001" } }
{ "$lte" : { "StartDate" : "2014-03-25" } }
{ "$gt"  : { "PI" : 3.14 } }
{ "$gte" : { "Count" : 3 } }
```

**Notes :**
- Ces opérateurs ne doivent être utilisés que pour les champs de type chaîne non analysée, date, nombre et booléen. Le comportement dans le cas d'un champ de type texte analysé ou null est non supporté.
- La comparaison doit se faire entre le même type. Le comportement dans le cas de types de données différents (par exemple comparer une date et un booléen) est non supporté.

### Opérateur $range : Recherche par intervalle de valeurs

Il s'agit d'un raccourci pour les opérateurs $lt, $lte, $gt et $gte pour effectuer des recherches sur un intervalle ouvert, semi-ouvert ou fermé.

**Format :**
`{ $range : { name : { minOperator : minValue, maxOperator : maxValue } } }` : où name est le nom du champ, minOperator est l'opérateur de comparaison ($gt ou $gte),  minValue est la valeur de comparaison minimale, maxOperator est l'opérateur de comparaison ($lt ou $lte),  maxValue est la valeur de comparaison maximale.

**Exemples :**

```json
{ "$range" : { "Identifier" : { "$gte" : "CT-000001", "$lte" : "CT-000009" } } }
{ "$range" : { "StartDate" : { "$gt" : "2014-03-25", "$lt" : "2014-04-25" } } }
{ "$range" : { "Count" : { "$gte" : 0, "$lt" : 10 } } }
```

**Notes :**
- Cet opérateur ne doit être utilisé que pour les champs de type chaîne non analysée, date, nombre et booléen. Le comportement dans le cas d'un champ de type texte analysé ou null est non supporté.
- La comparaison doit se faire entre le même type. Le comportement dans le cas de types de données différents (par exemple comparer une date et un booléen) est non supporté.
- Aucune vérification n'est effectuée quant aux valeurs passées dans les *$gt* et *$lt* (ou encore *$gte* et *$lte*), ce qui signifie qu'en cas de mauvais range (ex: "$gt" : "2014-04-25", "$lt" : "2014-04-24") la query sera considérée correcte, mais aucun résultat ne sera retourné.
- Les dates dans VITAM étant la plupart du temps au format ISO, pour rechercher des Units ayant leur StartDate sur un jour donné, il convient donc d'utiliser une Query range.


### Opérateur $exists : test d'existence d'au moins une valeur non nulle dans un champ.

**Format :**
- `{ "$exists" : name }` : où name est le nom du champ à vérifier.

**Exemples :**

L'application de la requête suivante : `{ "$exists" : "Data" }` sélectionne les documents suivants :

```json
{ "Data": false }              /* Booléen */
{ "Data": "2017-01-01" }       /* Date */
{ "Data": "" }                 /* chaîne ou texte vide */
{ "Data": "DATA" }             /* chaîne ou texte non vide */
{ "Data": [ "DATA" ] }         /* Tableau contenant au moins une valeur non nulle */
{ "Data": [ "DATA", null ] }   /* Tableau contenant au moins une valeur non nulle */
```

L'application de la même requête ( `{ "$exists" : "Data" }` ) ne sélectionne pas les documents suivants :

```json
{ "Data": null }               /* Champ null */
{ "Data": [ ] }                /* Tableau vide */
{ "Data": [ null ] }           /* Tableau vide */
{ "PasDeChampData" }           /* Champ inexistant */
```

### Opérateurs $in, $nin : recherche dans une liste de valeurs

**Format :**
- `{ "$in"  : { name : [ value1, value2, ... ] } }` : où name est le nom du champ, valueN les valeurs recherchées. (Recherche de présence parmi les valeurs du tableau, IN).
- `{ "$nin" : { name : [ value1, value2, ... ] } }` : où name est le nom du champ, valueN les valeurs recherchées. (Recherche de non présence parmi les valeurs du tableau, NOT IN)

**Exemples :**

```json
{ "$in"  : { "Identifier" : [ "CT-000001", "CT-000002" ] } }
{ "$in"  : { "StartDate" : [ "2014-03-25", "2014-03-26" ] } }
{ "$nin" : { "HierarchyLevel" : [ 1, 2 ] } }
```

**Notes :**
- Ces opérateurs ne doivent être utilisés que pour les champs de type chaîne non analysée, date, nombre et booléen. Le comportement dans le cas d'un champ de type texte analysé ou null est non supporté.
- La comparaison doit se faire entre le même type. Le comportement dans le cas de types de données différents (par exemple comparer une date et un booléen) est non supporté.

## Opérateur $wildcard : recherche via une expression générique

Recherche via une expression générique (wildcard).

**Format :**
- `{ "$wildcard" : { name : expression } }` : où name est le nom du champ, expression l'expression recherchée.

Les wildcards autorisés dans *expression* sont :
- **« * »** : qui correspond à toute séquence de caractères, vide inclus.
- **« ? »** : qui correspond à un caractères unique

**Exemple :**
Recherche de tous les documents dont le champ DescriptionLevel commence par "Re" et se termine par "grp" :
```json
{ "$wildcard": { "DescriptionLevel": "Re*Grp" } }
```

**Notes :**
- Les expressions génériques peuvent être très lentes et très coûteuses.
- Cet opérateur ne doit être utilisé que pour les champs de type chaîne non analysée, date, nombre et booléen. Le comportement dans le cas d'un champ de type texte analysé ou null est non supporté.


### $regex : recherche via une expression régulière
La syntaxe utilisée est celle du moteur d'indexation elasticsearch.

**Format :**
- `{ "$regex" : { name : regex } }` : où *name* est le nom du champ et *regex* l'expression régulière recherchée.

**Exemple :**
Recherche de tous les documents dont le champ Identifier commence par ABCD suivis par un ou plusieurs chiffres :
```json
{ "$wildcard": { "Identifier": "ABCD[0-9]+" } }
```

**Notes :**
- Les expressions régulères peuvent être très lentes et très coûteuses.
- Cet opérateur ne doit être utilisé que pour les champs de type chaîne non analysée, date, nombre et booléen. Le comportement dans le cas d'un champ de type texte analysé ou null est non supporté.

### Opérateurs $match, $match_all, $match_phrase, $match_phrase_prefix : Recherche full-text

Recherche plein texte sur des mots clés.

**Format :**
- `$match : { name : words }` : où *name* est le nom du champ et *words* les mots recherchés, avec un opérator OR entre chaque mots. *$match* cherche donc **au moins un** mot spécifié dans les *words*, dans n'importe quel ordre. 
- `$match_all : { name : words }` où *name* est le nom du champ, *words* les mots recherchés, dans n'importe quel ordre, avec un opérateur AND entre chaque mots. *$match_all* cherche **tous** les mots spécifiés dans les *words*, dans n'importe quel ordre. 
- `$match_phrase : { name : words }` où *name* est le nom du champ et *words* les mots recherchés, avec un opérator AND entre chaque mots. De plus *$match_phrase_prefix* cherche **tous** les mot spécifiés dans les *words* en tenant également compte de **l'ordre des mots**.
- `$match_phrase_prefix : { name : words }` où *name* est le nom du champ et *words* les mots recherchés, avec un opérator AND entre chaque mots. De plus *$match_phrase_prefix* cherche **tous** les mot spécifiés dans les *words* en tenant également compte de **l'ordre des mots**. La recherche sur le dernier mot est de type prefixe. Cet opérateur peut être particulièrement adapté à la recherche avec auto-complétion 'as you type'.

**Note :**
- Ces opérateurs ne doivent être utilisés que pour les champs de type texte analysé. Le comportement dans le cas d'un champ de type chaîne non analysée, date, nombre, booléen ou null est non supporté.

**Exemples :**

Recherche dans un champ "Title": "Voyez ce koala fou qui mange des journaux et des photos dans un bungalow"

Pour $match :

```json
{ "$match" : { "Title" : "koala fou" } }                      /* OK */
{ "$match" : { "Title" : "fou koala" } }                      /* OK (pas d'ordre des mots) */
{ "$match" : { "Title" : "koala chocolat" } }                 /* OK (correspondance partielle) */
{ "$match" : { "Title" : "Dessert chocolat" } }               /* KO (aucun mot trouvé) */
```

Pour $match_all :

```json
{ "$match_all" : { "Title" : "koala fou" } }                  /* OK */
{ "$match_all" : { "Title" : "fou koala" } }                  /* OK (pas d'ordre des mots) */
{ "$match_all" : { "Title" : "koala chocolat" } }             /* KO (Pas de correspondance partielle) */
{ "$match_all" : { "Title" : "Dessert chocolat" } }           /* KO (Pas de correspondance) */
```

Pour $match_phrase :

```json
{ "$match_phrase" : { "Title" : "koala fou" } }               /* OK */
{ "$match_phrase" : { "Title" : "fou koala" } }               /* KO (l'ordre des mots n'est pas respecté) */
{ "$match_phrase" : { "Title" : "koala chocolat" } }          /* KO (Pas de correspondance partielle) */
{ "$match_phrase" : { "Title" : "Dessert chocolat" } }        /* KO (Pas de correspondance) */
```

Pour $match_phrase_prefix

```json
{ "$match_phrase_prefix" : { "Title" : "koala fou" } }        /* OK */
{ "$match_phrase_prefix" : { "Title" : "koala f" } }          /* OK (Correspondance avec préfixe sur le dernier terme) */
{ "$match_phrase_prefix" : { "Title" : "fou koala" } }        /* KO (Ordre non respecté) */
{ "$match_phrase_prefix" : { "Title" : "koala chocolat" } }   /* KO (Pas de correspondance partielle */
{ "$match_phrase_prefix" : { "Title" : "Dessert chocolat" } } /* KO (Pas de correspondance) */
```

### $search : recherche approchante avec opérateurs

Permet des recherches approchantes avec des expression exactes, des opérateurs ET  / OU / NON, préfixes...

**Format :**
`{ "$search" : { name : searchParameter } }` : où *name* est le nom du champ, *searchParameter* est une expression de recherche.

L'expression *searchParameter* peut être formulée avec les opérateurs suivants :
- **« + »** signifie **AND**
- **« | »** signifie **OR**
- **« - »** signifie **NOT** (tout sauf)
- **« " »** signifie **"expression exacte"** (l'ordre des mots est impératif dans la recherche)
- **« ( »** et **« ) »** signifie une précédence dans les opérateurs (priorisation des recherches **AND** et **OR**)
- **« * »** à la fin d'un mot signifie une recherche par préfixe.
  - La recherche est effecuté sur les racines des mots. En particulier, l'utilisation du **« * »** empêche les recherches avec des accents. Ainsi un search de "éco*" ne trouvera pas "école", en revanche "eco" trouvera "école".
 - ne pas confondre avec l'opérateur **$wildcard** qui permet de chercher des expression sur champs de type chaine non analysée.
- **« ~N »** à la fin d'un mot permet de réaliser une recherche approchante, *N* étant la distance d'édition (nombre d'insertions, de suppressions ou de substitutions nécessaires pour transformer un nom en un autre)
- **« ~N »** après une expression (encadrée par **« * »**) autorise des "trous" dans l'expression recherchée, *N* étant le nombre de mots maximums autorisés pour la complétion

**Important** : Par défaut, *$search* effectue un OR entre chaque mot de l'expression de recherche.

**Exemples :**

```json
{ "$search" : { "Title" : "alpha bravo charlie" } }             /* cherche un titre contenant au moins un mot entre alpha, bravo et charlie */
{ "$search" : { "Title" : "alpha +bravo charlie" } }            /* cherche un titre contenant le mot bravo obligatoirement */
{ "$search" : { "Title" : "+alpha -bravo" } }                   /* cherche un titre contenant le mot alpha ou ne contenant pas le mot bravo */
{ "$search" : { "Title" : "+alpha +-bravo" } }                  /* cherche un titre contenant le mot alpha et ne contenant pas le mot bravo */
{ "$search" : { "Title" : "+alpha -\"bravo charlie\"" } }       /* cherche un titre contenant le mot alpha et ne contenant pas l'expression 'bravo charlie' */
{ "$search" : { "Title" : "+alpha +(bravo | charlie)" } }       /* cherche un titre contenant le mot alpha et contenant soit le mot bravo et/ou le mot charlie */
```

**Note :**
- Cet opérateur ne doit être utilisé que pour les champs de type chaîne analysée. Le comportement dans le cas d'un champ de type texte analysé ou null est non supporté.


### Cas particulièr : recherches par #id

Un cas particulier est traité dans ce paragraphe, il s'agit de la recherche par identifiant technique.
Sur les différentes collections, le champ #id est un champ obligatoire peuplé par Vitam. Il s'agit de l'identifiant unique du document (unité archivistique, groupe d'objets, différents référentiels...) représentée sous la forme d'une chaîne de 36 caractères correspondant à un GUID.
Il est possible de faire des requêtes sur ce champ, voici les opérateurs à privilégier dans ce cadre :
- { **"$eq" : { "#id" : value }** } : où value est la valeur recherchée sous forme d'un GUID
- { **"$in" : { "#id" : [ value1, value2, ... ] }** } : où valueN sont les valeurs recherchées sous forme de GUID.

Il est aussi possible, mais moins recommandé, de faire :
- { **"$ne" : { "#id" : value }** } : étant la recherche eq inversée
- { **"$nin" : { "#id" : [ value1, value2, ... ] }** } : étant la recherche in inversée

L'utilisation des autres opérateurs avec ce champ #id est non supportée.

## Filtres
Les filtres permettent de spécifier des comportement additionnels à la recherce :

- $limit: le nombre maximum d'items retournés (positionné à 10000 par défaut)
- $offset: la position de démarrage dans la liste retournée (positionné à 0 par défaut)
- $orderby: { fieldname: 1, fieldname: -1 } : permet de définir un tri ascendant (1) ou descendant (-1).
  - Pour un champ non analysé : l'ordre est lexicographique pour un texte, l'ordre est naturel pour un champ date ou nombre
  - Pour un champ analysé (plein texte), le tri n'est pas lexicographique mais basé sur le score de correspondance
  - L'ordre de déclaration des tris est respectés dans la réponse

**Note :**
Le nombre de résultats ne doit être être trop important (**$limit** + **$offset** > 10000+) au risque d'un comportement de recherche non déterminé.

## Projections
Par défaut, les requêtes de recherche DSL renvoient tous les champs des documents. Ce qui correspond à un `SELECT *` dans le language SQL.

Cependant, il est fortement recommandé de limiter les champs à retourner en les spécifiant dans la section **$projection**.
Ce qui correspond au `SELECT X, Y, Z` dans le langage SQL.

```json
{
  "$projection": {
    "$fields": {
      "Champ1": 1,
      "Champ2": 1
    }
  }
}
```

La valeur **1** indique que le champ est activé (renvoyé au client). Toute autre valeur est non supportée.

**Exemple :** Pour ne renvoyer que les champs Title et Description

```json
{
  "$projection": {
    "$fields": {
      "Title": 1,
      "Description": 1
    }
  }
}
```

**Remarque :**
- Pour la collection *unit*, il est possible de demander la construction de l'ensemble des règles héritées en utilisant une projection *spéciale* **$rules**. Si cette projection est utilisée, l'ensemble des champs de l'unité archivistique est remonté.
  Cette projection ne devrait être utilisée que dans le point d'API */units/{id}* (GET BY ID).


```json
{
  "$projection": { "$fields": { "$rules": 1 } }
}
```

## Actions
Dans le cas d'un update, les opérateurs suivants sont utilisables

| Opérateur | Commentaire                                         |
|-----------|-----------------------------------------------------|
| $set      | Crée ou modifie la valeur d'un ou plusieurs champs  |
| $unset    | Supprime un ou plusieurs champs                     |

# Response

Une réponse est composée de plusieurs parties :

- **$hits**:
  - **limit**: le nombre maximum d'items retournés (limité à 1000 par défaut)
  - **offset**: la position de démarrage dans la liste retournée (positionné à 0 par défaut)
  - **total**: le nombre total potentiel (estimation) des résultats possibles
  - **size**: le nombre réel d'items retournés
- **$context**: rapelle la requête exprimée
- **$results**: contient le résultat de la requête sous forme d'une liste d'items

## Exemples

### Réponse pour Units

```json
{
  "$hits": {
    "total": 3,
    "size": 3,
    "offset": 0,
    "limit": 100
  },
  "$context": {
    "$roots": [ "GUID0" ],
    "$query": [
      { "$match": { "Title": "titre" }, "$depth": 4 }
    ],
    "$filter": { "$limit": 100 },
    "$projection": { "$fields": { "#id": 1, "Title": 1 } },
  },
  "$results": [
    { "#id": "GUID1", "Title": "titre 1" },
    { "#id": "GUID2", "Title": "titre 2" },
    { "#id": "GUID3", "Title": "titre 3" }
  ]
}
```

## Réponse en cas d'erreurs

En cas d'erreur, Vitam retourne un message d'erreur dont le format est :

- **httpCode** : code erreur Http
- **code** : code erreur Vitam
- **context** : contexte de l'erreur
- **state** : statut en format de message court sous forme de code
- **message** : statut en format de message court
- **description** : statut détaillé
- **errors** : le cas échéant des sous-erreurs associées avec le même format


### Exemple de retour en erreur

```json
{
  "httpCode": 404,
  "code": "404",
  "context": "ADMIN_EXTERNAL",
  "state": "code_vitam",
  "message": "Not Found",
  "description": "Access contract not found with id: GUID0"
}
```

# Champs texte analysés et non analysés

Il existe 2 types de champs texte indexés sur le moteur d'indexation elasticsearch :

- **Les champs analysés :** Ces champs sont analysés syntaxiquement par le moteur d'indexation (full-text). Par exemple, les champs *Description*, *Name*.
- **Les champs non analysés :** Ces champs ne sont pas analysés par le moteur d'indexation (exact match). Par exemple, les champs *Identifier* ou *OriginatingAgency*.

La liste complète des champs analysés et non analysés est disponible dans la documentation du modèle de données.

# Champs spéciaux

Des champs sont protégés dans les requêtes :
- Il est interdit d'exprimer un champ qui démarre par un *'_'*
- La plupart de ces champs protégés sont interdits à la modification. Ils ne sont utilisables que dans la partie *$projection* ou *$query* mais pas dans la partie *$action*
- Communs Units et Objects
  - **#id** est l'identifiant de l'item
  - **#unitups** est la liste des Units parents
  - **#tenant** est le tenant associé
  - **#operations** est la liste des opérations qui ont opéré sur cet élément
  - **#originating_agency** est l'OriginatingAgency su SIP d'origine
  - **#originating_agencies** est l'ensemble des OriginatingAgencies issues du SIP et des rattachements (héritage)
  - **#storage** est l'état de stockage
  - **#score** contiendra en cas de requête avec plein texte le score de pertinence (certaines collections n'auront pas ce champ)
  - **#version** version du document
- Spécifiques pour les Units
  - **#unittype** est la typologie du Unit (Arbre HOLLDING_UNIT, Plan FILING_UNIT ou ArchiveUnit INGEST)
  - **#nbunits** est le nombre de fils immédiats à un Unit donné
  - **#object** est l'objet associé à un Unit (s'il existe)
  - **#type** est le type d'item (Document Type)
  - **#allunitups** est l'ensemble des Units parents (depuis les racines)
  - **#management** est la partie règles de gestion associées au Unit (ce champ est autorisée à être modifiée et donc dans *$action*)
  - **#min** : est la profondeur minimum de l’unité archivistique par rapport à une racine.
  - **#max** : est la profondeur maximale de l’unité archivistique par rapport à une racine.
- Spécifiques pour les Objects
  - **#type** est le type d'item (Type d'Objet : Document, Audio, Video, Image, Text, ...)
  - **#nbobjects** est le nombre d'objets binaires (usages/version) associé à cet objet
  - **#qualifiers** est la liste des qualifiers disponibles
    - Les "qualifiers" disponibles pour les objets :
      - **PhysicalMaster** pour original physique
      - **BinaryMaster** pour conservation
      - **Dissemination** pour la version de diffusion compatible avec un accès rapide et via navigateur
      - **Thumbnail** pour les vignettes pour les affichages en qualité très réduite et très rapide en "prévue"
      - **TextContent** pour la partie native texte (ASCII UTF8)
    - Un raccourci exite : **#usage**
  - **#size** est la taille d'un objet
  - **#format** est le format (PUID) d'un objet
