package net.lopymine.ip.yacl.state;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.impl.ProvidesBindingForDeprecation;
import lombok.Getter;

public class PreviewStateManager<T> implements StateManager<T>, ProvidesBindingForDeprecation<T> {

	private final T previousValue;
	private T pendingValue;
	@Getter
	private final Binding<T> binding;
	private StateListener<T> stateListener;
	private boolean shouldSave = false;

	public PreviewStateManager(Binding<T> binding) {
		this.binding = binding;
		this.previousValue = binding.getValue();
		this.pendingValue = binding.getValue();
		this.stateListener = StateListener.noop();
	}

	// "fake" preview set
	public void set(T value) {
		boolean changed = !this.pendingValue.equals(value);
		boolean previousValue = this.previousValue.equals(value);

		this.binding.setValue(value);
		this.pendingValue = value;

		if (previousValue) {
			this.shouldSave = false;
		}
		if (changed && !previousValue) {
			this.shouldSave = true;
		}
		if (changed) {
			this.stateListener.onStateChange(this.pendingValue, value);
		}
	}

	// Current pending value
	public T get() {
		return this.pendingValue;
	}

	// Save current pending value
	public void apply() {
		this.shouldSave = false;
	}

	// reset to default
	public void resetToDefault(ResetAction action) {
		this.set(this.binding.defaultValue());
	}

	// forget this pending value
	public void sync() {
		this.set(this.previousValue);
	}

	// "Is new value was saved"
	public boolean isSynced() {
		return !this.shouldSave;
	}

	public boolean isDefault() {
		return this.binding.defaultValue().equals(this.pendingValue);
	}

	public void addListener(StateListener<T> stateListener) {
		this.stateListener = this.stateListener.andThen(stateListener);
	}

}

