package util;

public interface Command<E> 
{
	public void execute(E item);
}
