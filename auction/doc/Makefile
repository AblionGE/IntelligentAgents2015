report.pdf: *.md
	pandoc -r markdown $(shell cat report_orga) -t latex -o out.pdf --toc-depth=4 -s --highlight-style=tango
	mv out.pdf oeschger-schaer-ex5.pdf
clean: oeschger-schaer-ex5.pdf
	rm oeschger-schaer-ex5.pdf
monitor:
	when-changed *.md -c make
