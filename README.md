# simpleArk
A top-down implementation of a Rolonic Arc

## An Ark

An ark is a sorted map of rolons, keyed by the UUID which identifies each rolon.

## A Rolon

A rolon is a sorted map of rolon values, keyed by the type 1 UUID which identifies the
journal entry which created that value. The last value in the map is the current value of the rolon.

## A Rolon Value

A rolon value is a record with the following:

- ::journal-entry-uuid This is the same type 1 UUID which serves as the key to this rolon value.
- ::descriptors A sorted map of the descriptors of the rolon value.
- ::classifiers A sorted map of the classifiers of the rolon value.
- ::previous-value The previous rolon value.

## A Journal Entry

A journal entry is a rolon which reflects the creation of one or more rolons.
Only journal entries have type 1 UUIDs, allowing all journal entries to be
accessed in order via the sorted map of the ark.
A non-performing journal entry reflects only its own creation.

Performing journal entries have a classifier which holds a list of all the other
rolon values whose creation is reflected by this journal entry.

## An Index

An index is a rolon. Its UUID is created from the name of a classifier.
An index has a classifier for each value assigned to that classifier.
