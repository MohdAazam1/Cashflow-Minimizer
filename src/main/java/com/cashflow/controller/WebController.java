package com.cashflow.controller;

import com.cashflow.model.Person;
import com.cashflow.model.TransactionRecord;
import com.cashflow.repository.PersonRepository;
import com.cashflow.repository.TransactionRepository;
import com.cashflow.service.SettlementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class WebController {

    private final PersonRepository personRepo;
    private final TransactionRepository txRepo;
    private final SettlementService settlementService;

    public WebController(PersonRepository personRepo, TransactionRepository txRepo, SettlementService settlementService) {
        this.personRepo = personRepo;
        this.txRepo = txRepo;
        this.settlementService = settlementService;
    }

    @GetMapping({"/", "/index"})
    public String index(Model model){
        model.addAttribute("people", personRepo.findAll());
        model.addAttribute("transactions", txRepo.findAll());
        return "index";
    }

    @PostMapping("/person/add")
    public String addPerson(@RequestParam String name){
        if (name != null && !name.trim().isEmpty()){
            personRepo.save(new Person(name.trim()));
        }
        return "redirect:/";
    }

    @PostMapping("/transaction/add")
    public String addTransaction(@RequestParam Long payerId, @RequestParam Long payeeId, @RequestParam String amount){
        Person payer = personRepo.findById(payerId).orElse(null);
        Person payee = personRepo.findById(payeeId).orElse(null);
        if (payer!=null && payee!=null && !payer.getId().equals(payee.getId())) {
            try {
                BigDecimal amt = new BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP);
                TransactionRecord tr = new TransactionRecord(payer, payee, amt);
                txRepo.save(tr);
            } catch (Exception ex) {
                // ignore invalid amount
            }
        }
        return "redirect:/";
    }

    @PostMapping("/compute")
    public String compute(Model model){
        List<Person> people = personRepo.findAll();
        List<TransactionRecord> tx = txRepo.findAll();
        List<String> settlements = settlementService.computeSettlements(people, tx);
        model.addAttribute("people", people);
        model.addAttribute("transactions", tx);
        model.addAttribute("settlements", settlements);
        return "index";
    }

    @PostMapping("/reset")
    public String reset(){
        txRepo.deleteAll();
        personRepo.deleteAll();
        return "redirect:/";
    }
}
