# simpleArk
A top-down implementation of a Rolonic Arc

## An Ark

An ark is a collection of rolons, each with a unique UUID which does not change over time.
A UUID then establishes the identity of a rolon.

The ark is updated by processing a series of transactions,
where each transaction creates or modifies one or more rolons.

## A Rolon

There are 3 types of rolons: application rolons, journal entries
and indexes.

A rolon is a sorted map of values, ordered by time.
Each rolon value is keyed by a type-1 UUID (timestamp), which is
also the UUID of the transaction that created the value and the 
UUID of the journal entry used to record how the transaction was processed.

Type-4 UUID's (random) is used for application rolons while type-5
UUID's (text) are used for index rolons.

## A Rolon Value

A rolon value is a record with the following:

- ::journal-entry-uuid This is the same type 1 UUID which serves as the key to this rolon value.
- ::contents A sorted map of the descriptors and classifiers of the rolon value. 
Classifier names begin with ! while descriptor names begin with `.
The contents of the map is a vector holding the current value of the descriptor or classifier
and the type 1 UUID of the journal entry which last changed. 
A nil is used to represent a descriptor or classifier which has been removed.
- ::previous-value The previous rolon value, or nil.

## A Journal Entry

A journal entry is a rolon which reflects the creation of one or more rolons.
Only journal entries have type 1 UUIDs, allowing all journal entries to be
accessed in order via the sorted map of the ark.
A non-performing journal entry reflects only its own creation.

Performing journal entries have a classifier which holds a list of all the other
rolon values whose creation is reflected by this journal entry.

## An Index

An index is a rolon. Its UUID is created from the name of a classifier.
An index has a classifier which holds a map, keyed by classifier value,
of all rolons which have ever been assigned that value.
