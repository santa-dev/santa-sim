package santa.simulator.genomes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class Coordinates {


	public Coordinates() {
	}

	public void addFragment(int start, int finish) {
		fragments.add(new Fragment(start, finish));
	}

	public int getFragmentCount() {
		return fragments.size();
	}

	public int getFragmentStart(int index) {
		return fragments.get(index).getStart();
	}

	public int getFragmentFinish(int index) {
		return fragments.get(index).getFinish();
	}

	private final List<Fragment> fragments = new ArrayList<Fragment>();

	private class Fragment {
		public Fragment(int start, int finish) {
			this.start = start;
			this.finish = finish;
		}

		public int getStart() {
			return start;
		}

		public int getFinish() {
			return finish;
		}

		private final int start;
		private final int finish;
	}
}
