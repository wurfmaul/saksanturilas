package at.jku.chess.saksanturilas.move;


/**
 * Dummy class for representation of unrejectable remis
 * 
 * @author Fabian Jordan (0855941)
 * @author Wolfgang Kuellinger (0955711)
 * 
 */
public abstract class UnrejectableRemisMove extends RemisMove {
	/**
	 * Constructor for Move to force remis
	 */
	public UnrejectableRemisMove() {
		remis = REMIS_UNREJECTABLE;
	}
	
	@SuppressWarnings("unused")
	private UnrejectableRemisMove(boolean acceptRemis) {
		//Defined to hide the super constructor
	}
}