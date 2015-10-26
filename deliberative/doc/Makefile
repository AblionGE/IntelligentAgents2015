report.pdf: *.md
	pandoc -r markdown $(shell cat report_orga) -t latex -o out.pdf --toc-depth=4 -s --highlight-style=tango
	mv out.pdf oeschger-schaer-ex3.pdf
clean: oeschger-schaer-ex2.pdf
	rm oeschger-schaer-ex2.pdf
monitor:
	when-changed *.md -c make
