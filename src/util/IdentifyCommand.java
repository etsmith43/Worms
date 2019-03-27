package util;

public interface IdentifyCommand<E> extends Command<E>
{
	public int getResult();
}
