package com.cashflow.service;

import com.cashflow.model.Person;
import com.cashflow.model.TransactionRecord;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class SettlementService {

    public List<String> computeSettlements(List<Person> people, List<TransactionRecord> transactions) {
        Map<Long, BigDecimal> net = new HashMap<>();
        for (Person p : people) net.put(p.getId(), BigDecimal.ZERO);

        for (TransactionRecord t : transactions) {
            BigDecimal amt = t.getAmount();
            net.put(t.getPayer().getId(), net.get(t.getPayer().getId()).subtract(amt));
            net.put(t.getPayee().getId(), net.get(t.getPayee().getId()).add(amt));
        }

        // Lists for creditors and debtors
        PriorityQueue<PersonBalance> creditors = new PriorityQueue<>((a,b)->b.amount.compareTo(a.amount));
        PriorityQueue<PersonBalance> debtors = new PriorityQueue<>((a,b)->a.amount.compareTo(b.amount));

        Map<Long, Person> byId = new HashMap<>();
        for (Person p: people) byId.put(p.getId(), p);

        for (Map.Entry<Long, BigDecimal> e : net.entrySet()) {
            BigDecimal val = e.getValue().setScale(2, RoundingMode.HALF_UP);
            if (val.compareTo(BigDecimal.ZERO) > 0) creditors.add(new PersonBalance(byId.get(e.getKey()), val));
            else if (val.compareTo(BigDecimal.ZERO) < 0) debtors.add(new PersonBalance(byId.get(e.getKey()), val));
        }

        List<String> settlements = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            PersonBalance cr = creditors.poll();
            PersonBalance db = debtors.poll();

            BigDecimal settle = cr.amount.min(db.amount.abs());
            // debtor pays creditor
            settlements.add(String.format("%s pays %s to %s", db.person.getName(), settle.setScale(2, RoundingMode.HALF_UP).toString(), cr.person.getName()));

            cr.amount = cr.amount.subtract(settle).setScale(2, RoundingMode.HALF_UP);
            db.amount = db.amount.add(settle).setScale(2, RoundingMode.HALF_UP);

            if (cr.amount.compareTo(BigDecimal.ZERO) > 0) creditors.add(cr);
            if (db.amount.compareTo(BigDecimal.ZERO) < 0) debtors.add(db);
        }

        return settlements;
    }

    private static class PersonBalance {
        Person person;
        BigDecimal amount;
        PersonBalance(Person p, BigDecimal a){ this.person = p; this.amount = a; }
    }
}
