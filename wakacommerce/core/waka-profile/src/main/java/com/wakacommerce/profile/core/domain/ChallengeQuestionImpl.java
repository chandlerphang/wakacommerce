
package com.wakacommerce.profile.core.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.wakacommerce.common.i18n.service.DynamicTranslationProvider;
import com.wakacommerce.common.presentation.AdminPresentation;
import com.wakacommerce.common.presentation.AdminPresentationClass;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_CHALLENGE_QUESTION")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
@AdminPresentationClass(friendlyName = "ChallengeQuestionImpl_baseChallengeQuestion")
public class ChallengeQuestionImpl implements ChallengeQuestion {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "ChallengeQuestionId")
    @GenericGenerator(
        name="ChallengeQuestionId",
        strategy="com.wakacommerce.common.persistence.IdOverrideTableGenerator",
        parameters = {
            @Parameter(name="segment_value", value="ChallengeQuestionImpl"),
            @Parameter(name="entity_name", value="com.wakacommerce.profile.core.domain.ChallengeQuestionImpl")
        }
    )
    @Column(name = "QUESTION_ID")
    protected Long id;

    @Column(name = "QUESTION", nullable=false)
    @AdminPresentation(friendlyName = "ChallengeQuestionImpl_Challenge_Question", group = "ChallengeQuestionImpl_Customer")
    protected String question;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getQuestion() {
        return DynamicTranslationProvider.getValue(this, "question", question);
    }

    @Override
    public void setQuestion(String question) {
        this.question = question;
    }

    @Override
    public String toString() {
        return question;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((question == null) ? 0 : question.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!getClass().isAssignableFrom(obj.getClass()))
            return false;
        ChallengeQuestionImpl other = (ChallengeQuestionImpl) obj;

        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        if (question == null) {
            if (other.question != null)
                return false;
        } else if (!question.equals(other.question))
            return false;
        return true;
    }
}
