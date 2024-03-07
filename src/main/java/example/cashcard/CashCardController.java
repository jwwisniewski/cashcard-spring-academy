package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    private final CashCardRepository cashCardRepository;

    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal
    ) {
        Optional<CashCard> cashCardOptional = getOptionalCashCard(requestedId, principal);

        if (cashCardOptional.isPresent()) {
            return ResponseEntity.ok(cashCardOptional.get());
        }

        return ResponseEntity.notFound().build();
    }

    private Optional<CashCard> getOptionalCashCard(Long requestedId, Principal principal) {
        Optional<CashCard> cashCardOptional = Optional.ofNullable(
                cashCardRepository.findByIdAndOwner(requestedId, principal.getName())
        );
        return cashCardOptional;
    }

    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard cashCardRequest, UriComponentsBuilder ucb, Principal principal) {
        CashCard savedCashCard = createCashCardFromRequestDataAndSave(cashCardRequest, principal);
        URI location = ucb.path("cashcards/{id}").buildAndExpand(savedCashCard.id()).toUri();

        return ResponseEntity.created(location).build();

    }

    private CashCard createCashCardFromRequestDataAndSave(CashCard cashCardRequest, Principal principal) {
        return createCashCardFromRequestDataAndSave(cashCardRequest, principal, null);
    }

    private CashCard createCashCardFromRequestDataAndSave(CashCard cashCardRequest, Principal principal, Long id) {
        CashCard newCashCard = new CashCard(id, cashCardRequest.amount(), principal.getName());

        CashCard savedCashCard = cashCardRepository.save(newCashCard);
        return savedCashCard;
    }

    @GetMapping
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findAllByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                )
        );

        return ResponseEntity.ok(page.getContent());
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<CashCard> putCashCard(@PathVariable Long requestedId
            , @RequestBody CashCard cashCardRequest
            , Principal principal
    ) {
        Optional<CashCard> cashCardOptional = getOptionalCashCard(requestedId, principal);

        if (cashCardOptional.isPresent()) {
            CashCard cashCardOriginal = cashCardOptional.get();
            createCashCardFromRequestDataAndSave(cashCardRequest, principal, cashCardOriginal.id());
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal) {
        if (!cashCardRepository.existsByIdAndOwner(id, principal.getName())) {
            return ResponseEntity.notFound().build();
        }

        cashCardRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
