package util;

public interface CountCommand<E> extends Command<E>
{
	public int getCount();
}
