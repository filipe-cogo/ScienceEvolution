/*
 * Copyright (c) 2011 Marco Aurélio Graciotto Silva <magsilva@ironiacorp.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ironiacorp.computer;

public class ComputerSystem
{
	private static OperationalSystem currentOS;
	
	public static synchronized OperationalSystem getCurrentOperationalSystem()
	{
		if (currentOS == null) {
			OperationalSystemDetector detector = new OperationalSystemDetector();
			OperationalSystemType os = detector.detectCurrentOS();
			switch (os) {
				case AIX:
				case HPUX:
				case Irix:
				case Linux:
				case MacOS:
				case Solaris:
					currentOS =  new Unix();
					break;
				case Windows:
					currentOS =  new Windows();
					break;
				default:
					currentOS = null;
			}
		}
		
		return currentOS;
	}
	
	public static synchronized void reset()
	{
		currentOS = null;
	}
}
