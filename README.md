XMLSchemaDateTimeParser
=======================

Overview
--------

Lightweight parser for XMLSchema dateTime format (subset of ISO 8601) in Java.


Description
-----------

XMLSchema dateTime is a subset of ISO 8601 and its format can be describes as
below.

    '-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? ('Z' | ('+' | '-') hh ':' mm)?

XMLSchemaDateTimeParser can parse strings represented in the above format, but
there are some trivial points to note:

- The specification allows the year part to be a 5-or-more-digit number, but
  XMLSchemaDateTimeParser assumes only 4-digit numbers at the year part.
- The specification allows a leading hyphen ('-') to indicate B.C. years, but
  XMLSchemaDateTimeParser just ignores the negative sign.
- The specification does not mention the length of the millisecond part, but
  XMLSchemaDateTimeParser assumes that the lenth is in between 1 and 3.


License
-------

Apache License, Version 2.0.


Download
--------

    git clone git://github.com/TakahikoKawasaki/XMLSchemaDateTimeParser.git


Javadoc
-------

[XMLSchemaDateTimeParser Javadoc](http://takahikokawasaki.github.com/XMLSchemaDateTimeParser/index.html)


Example
-------

    Calendar calendar = XMLSchemaDateTimeParser.parse("2005-11-14T02:16:38Z");


See Also
--------

* [XMLSchema dateTime](http://www.w3.org/TR/xmlschema-2/#dateTime)
* [ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)


Author
------

Takahiko Kawasaki, Neo Visionaries Inc.
