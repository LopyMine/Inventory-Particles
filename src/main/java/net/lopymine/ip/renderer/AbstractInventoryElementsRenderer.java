package net.lopymine.ip.renderer;

import java.util.*;
import lombok.*;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.optimization.ParticlesDeletionMode;
import net.lopymine.ip.element.base.*;
import net.lopymine.ip.element.inventory.IInventoryElement;
import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.*;

@Getter
@Setter
public abstract class AbstractInventoryElementsRenderer<E extends IInventoryElement> extends TickElement {

	@Getter
	private final Collection<E> screenElements = getScreenElementsList();
	private final List<E> pendingElements = new ArrayList<>();
	private final RandomSource random = RandomSource.create();
	
	private boolean stoppedByInitializationReason;
	private boolean stoppedTicking;
	private final int ticksPerTick = 1;
	private int nextTick = 1;
	
	@Nullable
	private E hoveredElement;
	@Nullable
	private E selectedElement;

	public void render(GuiGraphicsExtractor context, float tickProgress) {
		this.hoveredElement = null;
		if (this.screenElements.isEmpty()) {
			return;
		}
		this.runSoft(() -> this.renderElements(context, tickProgress), "rendering_inventory_elements");
	}

	protected abstract void renderElements(GuiGraphicsExtractor context, float tickProgress);

	protected boolean shouldTick() {
		if (this.stoppedTicking || this.stoppedByInitializationReason) {
			return false;
		}

		super.tick();
		if (this.ticks < this.nextTick) {
			return false;
		}
		this.nextTick = this.ticks + this.ticksPerTick;
		return true;
	}

	protected void tickElements() {
		this.screenElements.removeIf((particle) -> {
			if (particle == null) {

				return true;
			}
			particle.tick();
			return particle.isDead() && !particle.isSelected();
		});
	}

	protected void pushPendingElements() {
		this.screenElements.addAll(this.pendingElements);
		this.pendingElements.clear();
	}

	public void addPendingElement(E element) {
		this.checkElementsLimit();
		this.pendingElements.add(element);
	}

	protected void checkElementsLimit() {
		InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();
		int difference = (this.screenElements.size() + 1) - config.getParticleConfig().getParticlesCountLimit();
		if (difference > 0) {
			this.clearParticlesForNewOnes(difference, config.getParticleConfig().getParticlesDeletionMode());
		}
	}

	protected void clearParticlesForNewOnes(int difference, ParticlesDeletionMode mode) {
		switch (mode) {
			case OLDEST -> {
				if (!(this.screenElements instanceof ArrayDeque<E> deque)) {
					return;
				}
				for (int i = 0; i < difference; i++) {
					deque.pollFirst();
				}
			}
			case RANDOM -> {
				if (!(this.screenElements instanceof ArrayList<E> list)) {
					return;
				}
				for (int i = 0; i < difference; i++) {
					list.remove(this.random.nextIntBetweenInclusive(0, list.size() - 1));
				}
			}
		}
	}

	public void clear() {
		this.screenElements.clear();
		this.pendingElements.clear();
		this.hoveredElement                = null;
		this.selectedElement               = null;
		this.stoppedByInitializationReason = true;
	}

	public void init() {
		this.stoppedByInitializationReason = false;
	}

	public void mouseClicked(int button) {
		if (button != 1) {
			return;
		}

		if (!this.isStoppedTicking()) {
			return;
		}

		if (this.selectedElement != null) {
			this.selectedElement.setSelected(false);
			this.selectedElement = null;
			this.playClickSound();
			return;
		}

		if (this.hoveredElement == null) {
			return;
		}

		this.hoveredElement.setSelected(true);
		this.selectedElement = this.hoveredElement;
		this.playClickSound();
	}

	protected void playClickSound() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	public void updateElementsPositions(double xCoefficient, double yCoefficient) {
		for (IInventoryElement element : this.screenElements) {
			element.setX(element.getX() * xCoefficient);
			element.setY(element.getY() * yCoefficient);
		}
		for (IInventoryElement element : this.pendingElements) {
			element.setX(element.getX() * xCoefficient);
			element.setY(element.getY() * yCoefficient);
		}
	}

	protected void runSoft(Runnable runnable, String action) {
		try {
			runnable.run();
		} catch (Exception e) {
			this.getLogger().error("[{}] Unexpected error!", action, e);
			InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();
			config.getMainConfig().setModEnabled(false);
			config.saveAsync();
			this.onException(e);

			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				MutableComponent text = Component.literal("[%s] ".formatted(this.getModName())).append(Component.literal("Unexpected error with id \"%s\", please report this issue with your game logs! Inventory Particles was automatically disabled to prevent spamming ^^".formatted(action)).withStyle(ChatFormatting.RED));
				//? if >=26.1 {
				player.sendSystemMessage(text);
				//?} else {
				/*player.displayClientMessage(text, false);
				 *///?}
			}
		}
	}

	protected void onException(Exception e) {

	}

	protected abstract String getModName();

	protected abstract MossyLogger getLogger();

	protected @NotNull Collection<E> getScreenElementsList() {
		return switch (InventoryParticlesConfig.getInstance().getParticleConfig().getParticlesDeletionMode()) {
			case OLDEST -> new ArrayDeque<>();
			case RANDOM -> new ArrayList<>();
		};
	}
}
