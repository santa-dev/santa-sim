/*
 * Created on Jul 17, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.genomes;

/**
 * @author kdforc0
 *
 * A genomic mutable sequence.
 */
public final class SimpleSequence implements Sequence {
	/*
		 * Internal representation: every byte corresponds to a nucleotide.
		 */
	private byte states[];

	/**
	 * Create a new sequence with given nucleotide length. The new sequence
	 * is initialized to all A's.
	 */
	public SimpleSequence(int length) {
		states = new byte[length];
	}

	/**
	 * Create a new sequence with the given nucleotides.
	 */
	public SimpleSequence(String nucleotides) {
		states = new byte[nucleotides.length()];

		for (int i = 0; i < nucleotides.length(); ++i) {
			setNucleotide(i,  Nucleotide.parse(nucleotides.charAt(i)));
		}
	}


	public SimpleSequence(byte[] states) {
		this.states = states;
	}

	public SimpleSequence(Sequence other) {
		states = new byte[other.getLength()];

		copyNucleotides(0, other, 0, other.getLength());
	}

	public SimpleSequence(SimpleSequence other) {
		states = new byte[other.getLength()];

		copyNucleotides(0, other, 0, other.getLength());
	}


	public SimpleSequence(Sequence other, int start, int length) {
		states = new byte[length];

		copyNucleotides(0, other, start, start + length);
	}

	public SimpleSequence(SimpleSequence other, int start, int length) {
		states = new byte[length];

		copyNucleotides(0, other, start, start + length);
	}

	public SimpleSequence(Sequence other, Feature feature) {

		byte[] nucleotides = new byte[feature.getNucleotideLength()];
		int k = 0;
		for (int i = 0; i < feature.getFragmentCount(); i++) {
			int start = feature.getFragmentStart(i);
			int finish = feature.getFragmentFinish(i);
			if (start < finish) {
				for (int j = start; j <= finish; j++) {
					nucleotides[k] = getNucleotide(j);
					k++;
				}
			} else {
				for (int j = finish; j >= start; j--) {
					nucleotides[k] = getNucleotide(j);
					k++;
				}
			}
		}

		if (feature.getFeatureType() == Feature.Type.AMINO_ACID) {
			states = new byte[feature.getLength()];

			int j = 0;
			for (int i = 0; i < states.length; i++) {
				states[i] = STANDARD_GENETIC_CODE[nucleotides[j]][nucleotides[j + 2]][nucleotides[j + 2]];
				j += 3;
			}
		} else {
			states = nucleotides;
		}

	}

	public Sequence getSubSequence(int start, int length) {
		return new SimpleSequence(this, start, length);
	}

	/* (non-Javadoc)
		 * @see santa.simulator.genomes.Sequence#getLength()
		 */
	public int getLength() {
		return states.length;
	}

	/* (non-Javadoc)
		 * @see santa.simulator.genomes.Sequence#getAminoacidsLength()
		 */
	public int getAminoAcidsLength() {
		return getLength() / 3;
	}

	/* (non-Javadoc)
		 * @see santa.simulator.genomes.Sequence#getNucleotide(int)
		 */
	public byte getNucleotide(int i) {
		return states[i];
	}

	public void setNucleotide(int i, byte state) {
		states[i] = state;
	}

	protected void copyNucleotides(int start, SimpleSequence source,
	                               int sourceStart, int sourceStop) {
		System.arraycopy(source.states, sourceStart, states, start, sourceStop - sourceStart);
	}

	protected void copyNucleotides(int start, Sequence source,
	                               int sourceStart, int sourceStop) {
		for (int i = 0; i < sourceStop - sourceStart; ++i) {
			setNucleotide(start + i, source.getNucleotide(sourceStart + i));
		}
	}

	/* (non-Javadoc)
		 * @see santa.simulator.genomes.Sequence#getAminoAcid(int)
		 */
	public byte getAminoAcid(int i) {
		int aa_i = i * 3;

		return STANDARD_GENETIC_CODE[getNucleotide(aa_i)]
				[getNucleotide(aa_i + 1)]
				[getNucleotide(aa_i + 2)];
	}

	/* (non-Javadoc)
		 * @see santa.simulator.genomes.Sequence#getNucleotides()
		 */
	public String getNucleotides() {
		StringBuffer result = new StringBuffer(getLength());
		result.setLength(getLength());

		for (int i = 0; i < getLength(); ++i) {
			result.setCharAt(i, Nucleotide.asChar(getNucleotide(i)));
		}

		return result.toString();
	}

	/* (non-Javadoc)
	 * @see santa.simulator.genomes.Sequence#getNucleotideStates()
	 */
	public byte[] getNucleotideStates() {
		byte[] result = new byte[getLength()];

		for (int i = 0; i < getLength(); ++i) {
			result[i] = getNucleotide(i);
		}

		return result;
	}

	/* (non-Javadoc)
		 * @see santa.simulator.genomes.Sequence#getAminoacids()
		 */
	public String getAminoAcids() {
		StringBuffer result = new StringBuffer(getAminoAcidsLength());
		result.setLength(getAminoAcidsLength());

		for (int i = 0; i < getAminoAcidsLength(); ++i) {
			result.setCharAt(i, AminoAcid.asChar(getAminoAcid(i)));
		}

		return result.toString();
	}

	/* (non-Javadoc)
	 * @see santa.simulator.genomes.Sequence#getAminoAcidStates()
	 */
	public byte[] getAminoAcidStates() {
		byte[] result = new byte[getAminoAcidsLength()];

		for (int i = 0; i < getAminoAcidsLength(); ++i) {
			result[i] = getAminoAcid(i);
		}

		return result;
	}

	/**
	 * Standard genetic code, generated by biojava.
	 */
	private static final byte STANDARD_GENETIC_CODE[][][] = {
			{ { AminoAcid.K /* AAA */,
					AminoAcid.N /* AAC */,
					AminoAcid.K /* AAG */,
					AminoAcid.N /* AAT */
			},
					{ AminoAcid.T /* ACA */,
							AminoAcid.T /* ACC */,
							AminoAcid.T /* ACG */,
							AminoAcid.T /* ACT */
					},
					{ AminoAcid.R /* AGA */,
							AminoAcid.S /* AGC */,
							AminoAcid.R /* AGG */,
							AminoAcid.S /* AGT */
					},
					{ AminoAcid.I /* ATA */,
							AminoAcid.I /* ATC */,
							AminoAcid.M /* ATG */,
							AminoAcid.I /* ATT */
					}
			},
			{ { AminoAcid.Q /* CAA */,
					AminoAcid.H /* CAC */,
					AminoAcid.Q /* CAG */,
					AminoAcid.H /* CAT */
			},
					{ AminoAcid.P /* CCA */,
							AminoAcid.P /* CCC */,
							AminoAcid.P /* CCG */,
							AminoAcid.P /* CCT */
					},
					{ AminoAcid.R /* CGA */,
							AminoAcid.R /* CGC */,
							AminoAcid.R /* CGG */,
							AminoAcid.R /* CGT */
					},
					{ AminoAcid.L /* CTA */,
							AminoAcid.L /* CTC */,
							AminoAcid.L /* CTG */,
							AminoAcid.L /* CTT */
					}
			},
			{ { AminoAcid.E /* GAA */,
					AminoAcid.D /* GAC */,
					AminoAcid.E /* GAG */,
					AminoAcid.D /* GAT */
			},
					{ AminoAcid.A /* GCA */,
							AminoAcid.A /* GCC */,
							AminoAcid.A /* GCG */,
							AminoAcid.A /* GCT */
					},
					{ AminoAcid.G /* GGA */,
							AminoAcid.G /* GGC */,
							AminoAcid.G /* GGG */,
							AminoAcid.G /* GGT */
					},
					{ AminoAcid.V /* GTA */,
							AminoAcid.V /* GTC */,
							AminoAcid.V /* GTG */,
							AminoAcid.V /* GTT */
					}
			},
			{ { AminoAcid.STP /* TAA */,
					AminoAcid.Y /* TAC */,
					AminoAcid.STP /* TAG */,
					AminoAcid.Y /* TAT */
			},
					{ AminoAcid.S /* TCA */,
							AminoAcid.S /* TCC */,
							AminoAcid.S /* TCG */,
							AminoAcid.S /* TCT */
					},
					{ AminoAcid.STP /* TGA */,
							AminoAcid.C /* TGC */,
							AminoAcid.W /* TGG */,
							AminoAcid.C /* TGT */
					},
					{ AminoAcid.L /* TTA */,
							AminoAcid.F /* TTC */,
							AminoAcid.L /* TTG */,
							AminoAcid.F /* TTT */
					}
			}
	};

	public int getLength(SequenceAlphabet alphabet) {
		return getLength() / alphabet.getTokenSize();
	}

	public byte getState(SequenceAlphabet alphabet, int i) {
		if (alphabet == SequenceAlphabet.NUCLEOTIDES)
			return getNucleotide(i);
		else
			return getAminoAcid(i);
	}

	public String getStateString(SequenceAlphabet alphabet) {
		if (alphabet == SequenceAlphabet.NUCLEOTIDES)
			return getNucleotides();
		else
			return getAminoAcids();
	}

	public byte[] getStates(SequenceAlphabet alphabet) {
		if (alphabet == SequenceAlphabet.NUCLEOTIDES)
			return getNucleotideStates();
		else
			return getAminoAcidStates();
	}
}
