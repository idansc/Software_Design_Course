// TODO: maybe cut down tokens in all the 'catch (StringIndexOutOfBoundsException e) {}'

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.cli;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

/**
 * <p><code>Parser</code> creates {@link CommandLine}s.</p>
 *
 * @author John Keyes (john at integralsource.com)
 * @see Parser
 * @version $Revision: 551815 $
 */
public abstract class Parser implements CommandLineParser {

	// We should have created a new exception class instead of using StringIndexOutOfBoundsException, but 
	// we can't spare those tokens :(
	StringIndexOutOfBoundsException dummyException = new StringIndexOutOfBoundsException();
	
	class UnchekdExceptionWrapper extends RuntimeException
	{
		ParseException innerExc;
		UnchekdExceptionWrapper(ParseException e) { innerExc = e; }
	}
	
    /** commandline instance */
    private CommandLine _cmd;

    /** current Options */
    private Options _options;

    /** list of required options strings */
    private List _requiredOptions;

    /**
     * <p>Subclasses must implement this method to reduce
     * the <code>arguments</code> that have been passed to the parse 
     * method.</p>
     *
     * @param opts The Options to parse the arguments by.
     * @param arguments The arguments that have to be flattened.
     * @param stopAtNonOption specifies whether to stop 
     * flattening when a non option has been encountered
     * @return a String array of the flattened arguments
     */
    protected abstract String[] flatten(Options opts, String[] arguments, 
                                        boolean stopAtNonOption);

    /**
     * <p>Parses the specified <code>arguments</code> 
     * based on the specifed {@link Options}.</p>
     *
     * @param options the <code>Options</code>
     * @param arguments the <code>arguments</code>
     * @return the <code>CommandLine</code>
     * @throws ParseException if an error occurs when parsing the
     * arguments.
     */
    public CommandLine parse(Options options, String[] arguments)
                      throws ParseException
    {
        return parse(options, arguments, null, false);
    }

    /**
     * Parse the arguments according to the specified options and
     * properties.
     *
     * @param options the specified Options
     * @param arguments the command line arguments
     * @param properties command line option name-value pairs
     * @return the list of atomic option and value tokens
     *
     * @throws ParseException if there are any problems encountered
     * while parsing the command line tokens.
     */
    public CommandLine parse(Options options, String[] arguments, 
                             Properties properties)
        throws ParseException
    {
        return parse(options, arguments, properties, false);
    }

    /**
     * <p>Parses the specified <code>arguments</code> 
     * based on the specifed {@link Options}.</p>
     *
     * @param options the <code>Options</code>
     * @param arguments the <code>arguments</code>
     * @param stopAtNonOption specifies whether to stop 
     * interpreting the arguments when a non option has 
     * been encountered and to add them to the CommandLines
     * args list.
     *
     * @return the <code>CommandLine</code>
     * @throws ParseException if an error occurs when parsing the
     * arguments.
     */
    public CommandLine parse(Options options, String[] arguments, 
                             boolean stopAtNonOption)
        throws ParseException
    {
        return parse(options, arguments, null, stopAtNonOption);
    }

    /**
     * Parse the arguments according to the specified options and
     * properties.
     *
     * @param options the specified Options
     * @param arguments the command line arguments
     * @param properties command line option name-value pairs
     * @param stopAtNonOption stop parsing the arguments when the first
     * non option is encountered.
     *
     * @return the list of atomic option and value tokens
     *
     * @throws ParseException if there are any problems encountered
     * while parsing the command line tokens.
     */
    public CommandLine parse(Options options, String[] arguments, 
                             Properties properties, boolean stopAtNonOption)
        throws ParseException
    {
        // initialise members
        _options = options;

        // clear out the data in options in case it's been used before (CLI-71)
        options.helpOptions().stream().forEachOrdered(x -> ((Option)x).clearValues());

        _requiredOptions = options.getRequiredOptions();
        _cmd = new CommandLine();

        if (arguments == null)
            arguments = new String[0];

        List tokenList = Arrays.asList(flatten(_options, 
                                               arguments, 
                                               stopAtNonOption));

        ListIterator iterator = tokenList.listIterator();

        // process each flattened token
		try {
			iterator.forEachRemaining(currStr -> {
				String t = (String) currStr;

				// the value is the double-dash
					if ("--".equals(t))
						throw dummyException;

					// the value is a single dash
					if ("-".equals(t)) {
						if (stopAtNonOption)
							throw dummyException;
						_cmd.addArg(t);
						return;
					}

					if (stopAtNonOption && !options.hasOption(t)) {
						_cmd.addArg(t);
						throw dummyException;
					}
					// the value is an option
					if (t.startsWith("-")) {
						// get the option represented by arg
						// TODO::: CAN I CHECK ASSIGNEMENT?
						Option opt;
						// if there is no option throw an
						// UnrecognisedOptionException
						if ((opt = _options.getOption(t)) == null)
							throw new UnchekdExceptionWrapper(new UnrecognizedOptionException(
									"Unrecognized option: " + t));

						// if the option is a required option remove the option
						// from
						// the requiredOptions list
					_requiredOptions.remove(opt.getKey());

					// if the option is in an OptionGroup make that option the
					// selected
					// option of the group
					OptionGroup group;
					if ((group = _options.getOptionGroup(opt)) != null)
						try {
							group.setSelected(opt);
						} catch (ParseException e) { throw new UnchekdExceptionWrapper(e);}
					_requiredOptions.remove(group);

					// if the option takes an argument value
					if (opt.hasArg()) {
						// loop until an option is found
						try {
							iterator.forEachRemaining(tmpCurrStr -> {
								String str = (String) tmpCurrStr;

								// found an Option, not an argument
								if (_options.hasOption(str)
										&& str.startsWith("-")) {
									iterator.previous();
									throw dummyException;
								}

								// found a value
								try {
									opt.addValueForProcessing(Util
											.stripLeadingAndTrailingQuotes(str));
								} catch (RuntimeException exp) {
									iterator.previous();
									throw dummyException;
								}
							});
						} catch (StringIndexOutOfBoundsException e) {
						} 

						if ((opt.getValues() == null) && !opt.hasOptionalArg())
							throw new UnchekdExceptionWrapper( new MissingArgumentException(
									"Missing argument for option:"
											+ opt.getKey()));
					}

					// set the option on the command line
					_cmd.addOption(opt);
					return;
				}

				// the value is an argument
				_cmd.addArg(t);

				if (stopAtNonOption)
					throw dummyException;
			});
		} catch (StringIndexOutOfBoundsException e) {
		} catch (UnchekdExceptionWrapper e)
		{
			throw e.innerExc;
		}

		// eat the remaining tokens
		iterator.forEachRemaining(currStr -> {

			String str = (String) currStr;
			// ensure only one double-dash is added
			if (!"--".equals(str))
				_cmd.addArg(str);
		});

		// process properties:
		if (properties != null) {
			// TODO: try to improve.
			// for (String option : properties.stringPropertyNames()) {
			properties.stringPropertyNames().stream().anyMatch(option -> {
				if (_cmd.hasOption(option))
					return false;
				Option opt = _options.getOption(option);
				String value = properties.getProperty(option);

				if (opt.hasArg() && (opt.getValues() == null))
					opt.addValueForProcessing(value);
				else if (!(value.matches("[Yy][Ee][Ss]|[Tt][Rr][Uu][Ee]|1")))
					return true;
				_cmd.addOption(opt);
				return false;
			});
		}
		
        if (_requiredOptions.size() <= 0)
			return _cmd;
		
        
        // there are required options that have not been processsed
	    
	    StringBuffer buff = new StringBuffer("Missing required option");
	    buff.append(_requiredOptions.size() == 1 ? ": " : "s: ");
	
	    // loop through the required options
	    _requiredOptions.stream().forEachOrdered(x -> buff.append(x));
	
	    throw new MissingOptionException(buff.toString());
        
    }
}
