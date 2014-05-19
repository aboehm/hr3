/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ngonga
 */
public class Hypercube {
	public Set<Point> elements;

	public Hypercube() {
		elements = new HashSet<Point>();
	}

	public Set<Point> getElements() {
		return this.elements;
	}
}
