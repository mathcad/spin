package org.infrastructure.validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.FieldError;

public class ValidateResponseEntity {
	
	private List<ErrorWrapper> errors;
	
	private Boolean isValid;
	
	private Object message;
	
	@SuppressWarnings("unchecked")
	public ValidateResponseEntity(MessageSourceAccessor messageSourceAccessor, List<FieldError>... errors) {
		if (errors == null) {
			this.errors = Collections.EMPTY_LIST;
			this.isValid = true;
		} else {
			this.errors = new ArrayList<ErrorWrapper>();
			this.isValid = false;
			for (List<FieldError> list : errors) {
				for (FieldError error : list) {
					try {
						this.errors.add(new ErrorWrapper(error.getCode(),
								error.getField(), messageSourceAccessor.getMessage(error)));
					} catch (NoSuchMessageException e) {
						this.errors.add(new ErrorWrapper(
								error.getCode(),error.getField(), error.getDefaultMessage()));
					}
				}
			}
		}
		if (this.errors.size() == 0) {
			this.isValid = true;
		}
	}

	public List<ErrorWrapper> getErrors() {
		return errors;
	}

	public Boolean getIsValid() {
		return isValid;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}
}
