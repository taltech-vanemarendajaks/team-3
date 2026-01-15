# Tehnilise võla analüüs ja parendussoovitused

## Ülevaade

See dokument sisaldab Borsibaari projekti koodibaasi analüüsi tehnilise võla, struktuuriprobleemide, parimate praktikatega vastuolude ja võimalike parenduste osas.

---

## 🔴 Kriitilised probleemid

### 1. TypeScripti tüübikontroll on production build’is välja lülitatud

**Fail:** `frontend/next.config.ts`

```typescript
typescript: {
    ignoreBuildErrors: true,
}
```

**Probleem:** TypeScripti vead ignoreeritakse build’i ajal, mis võib viia production’is runtime vigadeni.

**Soovitus:** Eemalda `ignoreBuildErrors: true` ja paranda kõik tüübivead. Kasuta TypeScripti ranget režiimi.

**Prioriteet:** KRIITILINE

---

### 2. System.out.println kasutamine logimise asemel

**Failid:**
- `backend/src/main/java/com/borsibaar/controller/InventoryController.java` (read 47–49)
- `backend/src/main/java/com/borsibaar/controller/OrganizationController.java` (rida 38)
- `backend/src/main/java/com/borsibaar/jobs/PriceCorrectionJob.java` (read 32, 36, 88)

**Probleem:** `System.out.println` kasutamine logimiseks:
- ei võimalda juhtida logitaset
- ei ole struktureeritud
- production’is ei saa mugavalt filtreerida
- hajussüsteemides on keeruline jälgida

**Soovitus:** Asenda SLF4J/Logbackiga, kasutades Lomboki `@Slf4j`:

```java
@Slf4j
public class InventoryController {
    log.debug("Received request: {}", request);
    log.info("Processing stock addition for product: {}", request.productId());
}
```

**Prioriteet:** KÕRGE

---

### 3. organizationId dubleerimine Inventory-s

**Fail:** `backend/src/main/java/com/borsibaar/entity/Inventory.java`

**Probleem:**
- `Inventory` hoiab `organizationId`, kuigi see on juba `Product`-is olemas
- Väljal on `@Deprecated` ja TODO, kuid see on endiselt kasutuses
- Andmete sünkroonist väljaminemise risk
- Keerukam hooldus

**Soovitus:**
1. Eemalda `organizationId` väli `Inventory`-st
2. Kasuta `inventory.getProduct().getOrganizationId()` ligipääsuks
3. Uuenda kõik repo päringud
4. Loo Liquibase migratsioon veeru eemaldamiseks

**Prioriteet:** KÕRGE

---

## 🟠 Olulised probleemid

### 4. Ärireeglite valideerimise puudumine DTO-des

**Failid:** erinevad DTO-d

**Probleem:**
- `ProductRequestDto` sisaldab baastasemel valideerimist, kuid puudub kontroll `minPrice <= basePrice <= maxPrice`
- Mõnes DTO-s puudub negatiivsete koguste valideerimine
- Puuduvad “mõistlikkuse” piirid (nt hind ei saa olla 0.0001)

**Soovitus:**

```java
public record ProductRequestDto(
    @NotBlank String name,
    @NotNull @DecimalMin("0.01") BigDecimal currentPrice,
    @NotNull @DecimalMin("0.01") BigDecimal minPrice,
    @NotNull @DecimalMin("0.01") BigDecimal maxPrice,
    @NotNull Long categoryId
) {
    @AssertTrue(message = "minPrice must be <= currentPrice <= maxPrice")
    public boolean isValidPriceRange() {
        return minPrice.compareTo(currentPrice) <= 0 
            && currentPrice.compareTo(maxPrice) <= 0;
    }
}
```

**Prioriteet:** KESKMINE

---

### 5. N+1 probleem päringutes

**Fail:** `backend/src/main/java/com/borsibaar/service/InventoryService.java` (read 54–84)

**Probleem:**

```java
inventories.stream()
    .map(inv -> {
        Product product = productRepository.findById(inv.getProductId())
            .orElse(null); // N+1 query!
```

**Soovitus:**
- Kasuta `@EntityGraph` või JOIN FETCH-i repos
- Või lae kõik tooted ühe päringuga ja koosta Map

```java
@Query("SELECT i FROM Inventory i JOIN FETCH i.product WHERE i.organizationId = :orgId")
List<Inventory> findByOrganizationIdWithProduct(@Param("orgId") Long orgId);
```

**Prioriteet:** KESKMINE

---

### 6. Monoliitne Inventory komponent (1196 rida)

**Fail:** `frontend/app/(protected)/(sidebar)/inventory/page.tsx`

**Probleem:**
- Ühes failis on kogu inventari halduse loogika
- Läbi segi: data fetching, äriloogika, UI komponendid, vormid
- Raske testida ja hooldada
- Palju `@ts-expect-error` kommentaare

**Soovitus:** Jaga laiali:
- `hooks/useInventory.ts` – andmeloogika
- `components/inventory/InventoryTable.tsx` – tabel
- `components/inventory/ProductForm.tsx` – toote loomise vorm
- `components/inventory/StockModal.tsx` – modaalaknad
- `types/inventory.ts` – tüübid (või shared types)

**Prioriteet:** KESKMINE

---

### 7. Tüüpide jagamise puudumine Frontendi ja Backendi vahel

**Probleem:**
- TypeScripti tüübid on käsitsi kirjutatud ja võivad lahkneda
- Palju `@ts-expect-error` kommentaare (15+ kohta)
- Puudub “single source of truth” tüüpidel

**Soovitus:**
1. Genereeri TypeScripti tüübid OpenAPI/Swagger skeemist
2. Kasuta skeemi genereerimiseks `springdoc-openapi`
3. Genereeri tüübid `openapi-typescript` abil
4. Või loo shared package tüüpidega

**Prioriteet:** KESKMINE

---

### 8. Ebaühtlane vigade käsitlemine

**Probleem:**
- Mõned kontrollerid kasutavad `ResponseStatusException`
- Teised kasutavad kohandatud erandeid
- Frontend käsitleb vigu eri kohtades erinevalt
- Puudub tsentraliseeritud error boundary Reactis

**Soovitus:**
- Backend: kasuta ühtselt kohandatud erandeid `ApiExceptionHandler` kaudu
- Frontend: loo `ErrorBoundary` komponent ja ühtne `useErrorHandler` hook
- Kasuta vigade kuvamiseks toast-teavitusi

**Prioriteet:** KESKMINE

---

### 9. Organisatsiooni käsitsi kontroll igas meetodis

**Probleem:**
- Iga teenuse meetod kontrollib `organizationId` käsitsi
- Koodi dubleerimine
- Lihtne unustada kontroll lisada

**Soovitus:** Loo AOP aspekt või kasuta Spring Security meetmetasemel turvet:

```java
@PreAuthorize("hasPermission(#productId, 'Product', 'READ')")
public ProductResponseDto getById(Long productId) { ... }
```

Või loo baasteenus kontrolliga:

```java
protected void validateOrganizationAccess(Long organizationId, Long resourceOrgId) {
    if (!organizationId.equals(resourceOrgId)) {
        throw new ForbiddenException("Access denied");
    }
}
```

**Prioriteet:** KESKMINE

---

### 10. Tehingutüüpide hardcode’itud stringid

**Failid:** erinevad teenused

**Probleem:**

```java
transaction.setTransactionType("SALE");
transaction.setTransactionType("PURCHASE");
transaction.setTransactionType("ADJUSTMENT");
```

**Soovitus:** Loo enum:

```java
public enum TransactionType {
    SALE, PURCHASE, ADJUSTMENT, RETURN, 
    TRANSFER_IN, TRANSFER_OUT, INITIAL
}
```

**Prioriteet:** MADAL

---

## 🟡 Struktuuri- ja arhitektuuriprobleemid

### 11. Vastutuste segunemine SalesService-s

**Fail:** `backend/src/main/java/com/borsibaar/service/SalesService.java`

**Probleem:**
- `SalesService` sisaldab dünaamilise hinnastamise loogikat
- Hinnastamise loogika on laiali `SalesService` ja `PriceCorrectionJob` vahel
- Puudub üks keskne koht hinnareeglite jaoks

**Soovitus:** Loo `PricingService`:

```java
@Service
public class PricingService {
    public BigDecimal calculatePriceAfterSale(
        Product product, 
        BigDecimal currentPrice, 
        Organization org
    ) {
        if (!product.getCategory().isDynamicPricing()) {
            return currentPrice;
        }
        BigDecimal newPrice = currentPrice.add(org.getPriceIncreaseStep());
        return newPrice.min(product.getMaxPrice());
    }
}
```

**Prioriteet:** KESKMINE

---

### 12. Ebaefektiivne käibe arvutus

**Fail:** `backend/src/main/java/com/borsibaar/service/InventoryService.java` (read 458–479)

**Probleem:**

```java
private BigDecimal calculateTotalRevenue(List<InventoryTransaction> transactions) {
    return transactions.stream()
        .map(transaction -> {
            return inventoryRepository.findById(transaction.getInventoryId())
                .flatMap(inventory -> productRepository.findById(inventory.getProductId()))
                .map(product -> {
                    // N+1 queries!
```

**Soovitus:**
- Kasuta repo päringus JOIN-i
- Või kasuta tehingu `priceBefore` välja (see on juba olemas)

**Prioriteet:** KESKMINE

---

### 13. Kommenteeritud kood production’is

**Fail:** `backend/src/main/java/com/borsibaar/service/SalesService.java` (read 74–80)

**Probleem:** Kommenteeritud kood tuleks eemaldada või selgitada

**Soovitus:** Eemalda või jäta git history’sse

**Prioriteet:** MADAL

---

### 14. Stringvõtmete kasutamine grupeerimiseks

**Fail:** `backend/src/main/java/com/borsibaar/service/InventoryService.java` (read 305–312)

**Probleem:**

```java
return userId.toString() + "|" + (stationId != null ? stationId.toString() : "null");
// hiljem parsitakse tagasi
String[] parts = entry.getKey().split("\\|");
```

**Soovitus:** Kasuta komposiitvõtit:

```java
record UserStationKey(UUID userId, Long stationId) {}

Map<UserStationKey, List<InventoryTransaction>> transactionsByKey = 
    transactions.stream()
        .collect(Collectors.groupingBy(t -> 
            new UserStationKey(t.getCreatedBy(), t.getBarStationId())
        ));
```

**Prioriteet:** MADAL

---

### 15. Frontendis puudub valideerimine

**Probleem:**
- Vormid ei valideeri enne päringu saatmist
- Puuduvad kontrollid negatiivsete hindade, tühjade väljade jms kohta
- Vead kuvatakse alles pärast serveri vastust

**Soovitus:**
- Kasuta `react-hook-form` + `zod` valideerimiseks
- Või `yup` skeemivalideerimiseks
- Kuva vead reaalajas

**Prioriteet:** KESKMINE

---

### 16. Race condition’ite käsitlemine puudub

**Probleem:**
- Sama toote paralleelsete müükide korral võivad tekkida race condition’id
- Puudub optimistlik lukustus (optimistic locking)
- See võib viia negatiivse laoseisuni

**Soovitus:** Lisa `@Version` `Inventory`-le:

```java
@Version
private Long version;
```

Ja käsitle teenuses `OptimisticLockingFailureException`.

**Prioriteet:** KÕRGE

---

### 17. Organisatsiooni ID avalik ligipääs kontrolleris

**Fail:** `backend/src/main/java/com/borsibaar/controller/InventoryController.java` (read 24–34)

**Probleem:**

```java
@RequestParam(required = false) Long organizationId
// If organizationId is provided, use it (for public access)
```

**Soovitus:**
- Kui see on avalikuks kasutuseks, loo eraldi endpoint `/api/public/inventory`
- Või kasuta eraldi kontrollerit teise autoriseerimisega
- Dokumenteeri selle lähenemise turvalisus

**Prioriteet:** KESKMINE

---

### 18. Struktureeritud logimise puudumine

**Probleem:**
- Puuduvad struktureeritud logid (JSON)
- Puuduvad correlation ID-d päringute jälgimiseks
- Ärikohtumisi on keeruline eristada tehnilistest logidest

**Soovitus:**
- Kasuta structured logging’ut (nt Logstash JSON encoder)
- Lisa MDC correlation ID jaoks
- Logi ärisündmusi eraldi tehnilistest

**Prioriteet:** KESKMINE

---

### 19. Mitteoptimaalsed päringud statistikas

**Fail:** `backend/src/main/java/com/borsibaar/service/InventoryService.java`

**Probleem:**
- `getUserSalesStats` ja `getStationSalesStats` teevad palju päringuid
- Saaks optimeerida ühe JOIN-iga päringuga

**Soovitus:** Loo natiivpäringud või kasuta `@EntityGraph`:

```java
@Query("SELECT t FROM InventoryTransaction t " +
       "JOIN FETCH t.inventory i " +
       "JOIN FETCH i.product p " +
       "WHERE t.transactionType = 'SALE' AND p.organizationId = :orgId")
List<InventoryTransaction> findSalesWithDetails(@Param("orgId") Long orgId);
```

**Prioriteet:** MADAL

---

### 20. Puhverdus (caching) puudub

**Probleem:**
- Kategooriad laetakse iga kord uuesti
- Organisatsioone laetakse korduvalt
- Puudub caching sageli küsitava info jaoks

**Soovitus:** Lisa Spring Cache:

```java
@Cacheable("categories")
public List<CategoryResponseDto> getAllByOrg(Long organizationId) { ... }
```

**Prioriteet:** MADAL

---

## 🟢 Koodikvaliteedi parendused

### 21. Optional’i ebaühtlane kasutamine

**Probleem:**
- Mõnes kohas kasutatakse `Optional.ofNullable().orElse()`
- Teistes kohtades otsesed null-kontrollid
- Puudub ühtne stiil

**Soovitus:** Ühtlusta lähenemine nullable väärtustega töötamisel

**Prioriteet:** MADAL

---

### 22. “Magic numbers” ja stringid

**Probleem:**
- `"SALE-" + System.currentTimeMillis()` – ID vorming
- `"REDUCE-" + System.currentTimeMillis()` – reference ID vorming
- Puuduvad konstandid nende vormingute jaoks

**Soovitus:**

```java
public class TransactionConstants {
    public static final String SALE_ID_PREFIX = "SALE-";
    public static final String REDUCE_ID_PREFIX = "REDUCE-";
}
```

**Prioriteet:** MADAL

---

### 23. API dokumentatsiooni puudumine

**Probleem:**
- Puudub Swagger/OpenAPI dokumentatsioon
- Ilma koodi lugemata on API-st raske aru saada
- Puuduvad päringu/vastuse näited

**Soovitus:** Lisa `springdoc-openapi`:

```java
@Operation(summary = "Add stock to inventory")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Stock added"),
    @ApiResponse(responseCode = "404", description = "Product not found")
})
```

**Prioriteet:** KESKMINE

---

### 24. Testid katavad peamiselt happy path’i

**Probleem:**
- Negatiivseid teste on vähe
- Puuduvad testid samaaegse ligipääsu (concurrent access) jaoks
- Puuduvad testid edge case’ide jaoks

**Soovitus:** Lisa testid:
- Negatiivsetele stsenaariumitele (ebapiisav laoseis, vale organisatsioon)
- Concurrent update’idele
- Edge case’idele (null väärtused, piirväärtused)

**Prioriteet:** KESKMINE

---

### 25. Mõõdikute ja monitooringu puudumine

**Probleem:**
- Puuduvad jõudlusmõõdikud
- Puudub päringute trace’imine
- Production’i probleeme on raske jälgida

**Soovitus:**
- Lisa Micrometer mõõdikute jaoks
- Integreeri Prometheus/Grafanaga
- Lisa health check’id

**Prioriteet:** KESKMINE

---

## 📋 Prioriteetide kokkuvõte

### Kriitilised (paranda kohe):
1. ✅ TypeScripti kontroll on build’is välja lülitatud
2. ✅ System.out.println kasutamine
3. ✅ organizationId dubleerimine Inventory-s

### Kõrged (paranda lähiajal):
4. ✅ Race condition’ite käsitlemise puudumine
5. ✅ Ärireeglite valideerimise puudumine
6. ✅ N+1 probleemid päringutes
7. ✅ Monoliitne Inventory komponent

### Keskmised (planeeri järgmisse sprinti):
8. ✅ Frontend–Backend tüüpide jagamise puudumine
9. ✅ Ebaühtlane vigade käsitlemine
10. ✅ Organisatsiooni käsitsi kontroll
11. ✅ Vastutuste segunemine teenustes
12. ✅ Frontendi valideerimise puudumine
13. ✅ Organisatsiooni ID avalik ligipääs
14. ✅ Struktureeritud logimise puudumine
15. ✅ API dokumentatsiooni puudumine
16. ✅ Testikatvuse parandamine
17. ✅ Mõõdikute puudumine

### Madalad (võib edasi lükata):
18. ✅ Tehingutüüpide hardcode’itud stringid
19. ✅ Kommenteeritud kood
20. ✅ Stringvõtmete kasutamine
21. ✅ Optional’i ebaühtlane kasutamine
22. ✅ Magic numbers
23. ✅ Mitteoptimaalne statistika päringuloogika
24. ✅ Caching’u puudumine

---

## 🎯 Rakendamise soovitused

1. **Alusta kriitilistest probleemidest** – need võivad põhjustada production’i tõrkeid
2. **Tee refaktoreerimine samm-sammult** – ära kirjuta kõike korraga ümber
3. **Lisa testid enne refaktoreerimist** – tagamaks muudatuste ohutus
4. **Dokumenteeri muudatused** – uuenda arhitektuuridokumente
5. **Code review** – kõik muudatused peaksid läbima ülevaatuse

---

*Dokument on koostatud koodibaasi analüüsi põhjal kuupäevaga 2025-01-13*

