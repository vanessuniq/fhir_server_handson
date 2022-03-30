package org.saintmartinhospital.legacy.repository.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.saintmartinhospital.legacy.domain.DocType_;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.legacy.domain.PersonDoc;
import org.saintmartinhospital.legacy.domain.PersonDoc_;
import org.saintmartinhospital.legacy.domain.Person_;
import org.saintmartinhospital.legacy.repository.PersonDAO;
import org.saintmartinhospital.legacy.service.bo.FindPersonByCriteriaBO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PersonDAOImpl implements PersonDAO {

  private static final String ID_PARAM = "id";
  private static final String NAME_PARAM = "name";
  private static final String LAST_NAME_PARAM = "lastName";
  private static final String GENDER_PARAM = "gender";
  private static final String BIRTH_DATE_PARAM = "birthDate";
  private static final String DOC_TYPE_ABREV_PARAM = "docTypeAbrev";
  private static final String DOC_VALUE_PARAM = "docValue";
  private static final String EMAIL_PARAM = "email";

  @PersistenceContext
  protected EntityManager entityManager;

  @Transactional
  @Override
  public Person findById(Integer id) {
    return id == null ? null : entityManager.find(Person.class, id);
  }

  @Transactional
  @Override
  public Person save(Person person) {
    if (person != null) {
      entityManager.persist(person);
      entityManager.flush();
    }
    return person;
  }

  @Transactional
  @Override
  public List<Person> findByCriteria(FindPersonByCriteriaBO criteria) {
    Validate.notNull(criteria);
    final Map<String, Object> params = new HashMap<>();

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Person> criteriaQuery = builder.createQuery(Person.class);
    Root<Person> person = criteriaQuery.from(Person.class);

    List<Predicate> predicates = new ArrayList<>();

    // Build id criteria
    if (criteria.getId() != null) {
      ParameterExpression<Integer> idParam = builder.parameter(Integer.class, ID_PARAM);
      params.put(ID_PARAM, criteria.getId());
      predicates.add(builder.equal(person.get(Person_.ID), idParam));
    }

    // Build name criteria
    if (StringUtils.isNotEmpty(criteria.getName())) {
      Expression<String> nameParam = builder.lower(builder.parameter(String.class, NAME_PARAM));
      params.put(NAME_PARAM, criteria.getName());
      predicates.add(builder.or(
          builder.equal(builder.lower(person.get(Person_.firstName)), nameParam),
          builder.equal(builder.lower(person.get(Person_.secondName)), nameParam)));
    }

    // Build last name criteria
    if (StringUtils.isNotEmpty(criteria.getLastName())) {
      Expression<String> lastNameParam = builder.lower(builder.parameter(String.class, LAST_NAME_PARAM));
      params.put(LAST_NAME_PARAM, criteria.getLastName());
      predicates.add(builder.equal(builder.lower(person.get(Person_.LAST_NAME)), lastNameParam));
    }

    // Build gender criteria
    if (criteria.getGender() != null) {
      params.put(GENDER_PARAM, criteria.getGender().toString());
      predicates.add(builder.equal(builder.lower(person.get(Person_.GENDER)),
          builder.lower(builder.parameter(String.class, GENDER_PARAM))));
    }

    // Build birth date criteria
    if (criteria.getBirthDate() != null) {
      params.put(BIRTH_DATE_PARAM, criteria.getBirthDate());
      predicates
          .add(builder.equal(person.get(Person_.BIRTH_DATE), builder.parameter(Calendar.class, BIRTH_DATE_PARAM)));
    }

    // Build email criteria
    if (criteria.getEmail() != null) {
      params.put(EMAIL_PARAM, criteria.getEmail());
      predicates.add(builder.equal(builder.lower(person.get(Person_.EMAIL)),
          builder.lower(builder.parameter(String.class, EMAIL_PARAM))));
    }

    // Build person document criteria
    if (StringUtils.isNotEmpty(criteria.getDocTypeAbrev()) && StringUtils.isNotEmpty(criteria.getDocValue())) {
      Expression<String> docTypeAbrevParam = builder.lower(builder.parameter(String.class, DOC_TYPE_ABREV_PARAM));
      Expression<String> docValueParam = builder.lower(builder.parameter(String.class, DOC_VALUE_PARAM));
      params.put(DOC_TYPE_ABREV_PARAM, criteria.getDocTypeAbrev());
      params.put(DOC_VALUE_PARAM, criteria.getDocValue());
      Join<Person, PersonDoc> personDoc = person.join(Person_.DOCS);
      predicates
          .add(builder.equal(builder.lower(personDoc.get(PersonDoc_.DOC_TYPE).get(DocType_.ABREV)), docTypeAbrevParam));
      predicates.add(builder.equal(builder.lower(personDoc.get(PersonDoc_.DOC_VALUE)), docValueParam));
    }

    // Create query
    criteriaQuery.select(person).where(predicates.toArray(new Predicate[predicates.size()]));
    final TypedQuery<Person> typedQuery = entityManager.createQuery(criteriaQuery);

    // Set parameters depending on which criteria was built
    CollectionUtils.emptyIfNull(params.keySet()).forEach(paramName -> {
      typedQuery.setParameter(paramName, params.get(paramName));
    });

    return typedQuery.getResultList();
  }

  @Override
  public Person attach(Person person) {
    return entityManager.merge(person);
  }

  @Override
  public List<Person> findByDocument(String typeAbrev, String docValue) {
    Validate.notEmpty(typeAbrev);
    Validate.notEmpty(docValue);

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Person> criteriaQuery = builder.createQuery(Person.class);
    Root<Person> person = criteriaQuery.from(Person.class);

    Expression<String> docTypeAbrevParam = builder.lower(builder.parameter(String.class, DOC_TYPE_ABREV_PARAM));
    Expression<String> docValueParam = builder.lower(builder.parameter(String.class, DOC_VALUE_PARAM));
    Join<Person, PersonDoc> personaDoc = person.join(Person_.DOCS);

    criteriaQuery.select(person).distinct(true).where(
        builder.equal(builder.lower(personaDoc.get(PersonDoc_.DOC_TYPE).get(DocType_.ABREV)), docTypeAbrevParam),
        builder.equal(builder.lower(personaDoc.get(PersonDoc_.DOC_VALUE)), docValueParam));
    TypedQuery<Person> typedQuery = entityManager.createQuery(criteriaQuery);
    typedQuery.setParameter(DOC_TYPE_ABREV_PARAM, typeAbrev).setParameter(DOC_VALUE_PARAM, docValue);

    return typedQuery.getResultList();
  }

}
