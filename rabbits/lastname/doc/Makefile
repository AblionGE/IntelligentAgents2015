report.pdf: *.md
	pandoc -r markdown $(shell cat report_orga) -t latex -o out.pdf --toc-depth=4 -s --highlight-style=tango
	mv out.pdf oeschger-schaer-in.pdf
clean: oeschger-schaer-in.pdf
	rm oeschger-schaer-in.pdf
monitor:
	when-changed *.md -c make
