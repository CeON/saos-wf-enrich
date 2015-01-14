(bob-module)

(load-clj-file "../../bob-common.clj")

(defrule
  [ [ "echo" 
      (inp ALL-COURT-JSON-FILES)
      ">" (out "out/file.lst") ]])
